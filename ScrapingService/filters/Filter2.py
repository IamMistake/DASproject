from filters.Filter import Filter
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import concurrent.futures
import os
from config import DB_CONFIG
import psycopg2
from psycopg2.extras import execute_batch


class SaveDataFilter(Filter):

    def process(self, issuers):
        print("Filter 2: Checking and saving issuer data...")

        cpu_cores = min(4, os.cpu_count() or 1)  # Limit to 4 cores max
        chunk_size = max(1, len(issuers) // cpu_cores)
        data_chunks = [issuers[i:i + chunk_size] for i in range(0, len(issuers), chunk_size)]

        with concurrent.futures.ProcessPoolExecutor(max_workers=cpu_cores) as executor:
            executor.map(self._process_chunk, data_chunks)

        return self._get_last_dates(issuers)

    def _process_chunk(self, issuers_chunk):
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        for issuer in issuers_chunk:
            if not self._has_data(issuer, cursor):
                self._download_issuer_history(issuer, cursor)

        conn.commit()
        cursor.close()
        conn.close()

    def _has_data(self, issuer, cursor):
        cursor.execute("""
            SELECT EXISTS(
                SELECT 1 FROM stock_data sd
                JOIN issuers i ON sd.issuer_id = i.issuer_id
                WHERE i.code = %s
                LIMIT 1
            )
        """, (issuer,))
        return cursor.fetchone()[0]

    def _download_issuer_history(self, issuer, cursor, years=10):
        print(f"Downloading {years} years of history for {issuer}")

        session = requests.Session()
        cursor.execute("SELECT issuer_id FROM issuers WHERE code = %s", (issuer,))
        issuer_id = cursor.fetchone()[0]

        for year in range(years):
            try:
                url = self._build_history_url(issuer, year)
                soup = self._get_soup(session, url)
                data = self._parse_soup_data(soup)

                if data:
                    self._store_data(issuer_id, data, cursor)
            except Exception as e:
                print(f"Error downloading {issuer} year {year}: {e}")

    def _build_history_url(self, issuer, year_offset):
        days = 364
        now = datetime.now()
        to_date = now - timedelta(days=days * year_offset)
        from_date = to_date - timedelta(days=days)

        return (
            f'https://www.mse.mk/mk/stats/symbolhistory/{issuer.lower()}'
            f'?FromDate={self._format_date(from_date)}'
            f'&ToDate={self._format_date(to_date)}'
            f'&Code={issuer}'
        )

    def _format_date(self, date):
        return date.strftime("%d.%m.%Y")

    def _get_soup(self, session, url):
        response = session.get(url, timeout=15)
        response.raise_for_status()
        return BeautifulSoup(response.text, 'html.parser')

    def _parse_soup_data(self, soup):
        table = soup.select_one('#resultsTable')
        if not table:
            return None

        rows = []
        for tr in table.select('tbody > tr'):
            cols = [td.text.strip() for td in tr.select('td')]
            if len(cols) >= 9:  # Ensure we have all expected columns
                rows.append(cols)
        return rows

    def _store_data(self, issuer_id, data, cursor):
        rows = []
        for row in data:
            try:
                rows.append((
                    issuer_id,
                    self._parse_date(row[0]),
                    self._parse_number(row[1]),
                    self._parse_number(row[2]),
                    self._parse_number(row[3]),
                    self._parse_number(row[4]),
                    self._parse_percent(row[5]),
                    self._parse_int(row[6]),
                    self._parse_number(row[7]),
                    self._parse_number(row[8]),
                ))
            except (ValueError, IndexError) as e:
                print(f"Skipping invalid row: {row} - {e}")
                continue

        execute_batch(cursor, """
            INSERT INTO stock_data (
                issuer_id, date, last_price, max_price, min_price,
                avg_price, percent_change, quantity, best_turnover, total_turnover
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (issuer_id, date) DO NOTHING
        """, rows)

    def _parse_date(self, date_str):
        return datetime.strptime(date_str, "%d.%m.%Y").date()

    def _parse_number(self, num_str):
        if num_str == '':
            num_str = '0'
        return float(num_str.replace('.', '').replace(',', '.'))

    def _parse_percent(self, percent_str):
        if percent_str == '':
            percent_str = '0'
        return float(percent_str.replace('%', '').replace(',', '.'))

    def _parse_int(self, int_str):
        return int(int_str.replace('.', ''))

    def _get_last_dates(self, issuers):
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        last_dates = {}
        for issuer in issuers:
            cursor.execute("""
                SELECT MAX(date) FROM stock_data sd
                JOIN issuers i ON sd.issuer_id = i.issuer_id
                WHERE i.code = %s
            """, (issuer,))
            result = cursor.fetchone()
            last_dates[issuer] = result[0] or datetime.now() - timedelta(days=365 * 10)

        cursor.close()
        conn.close()
        return last_dates
