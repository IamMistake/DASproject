import os

from filters.Filter import Filter
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import concurrent.futures
from config import DB_CONFIG
import psycopg2
from psycopg2.extras import execute_batch


class DataCompletenessFilter(Filter):

    def process(self, last_dates):
        print("Filter 3: Completing missing data...")

        cpu_cores = min(4, os.cpu_count() or 1)  # Limit to 4 cores max
        chunks = self._split_dict(last_dates, cpu_cores)

        with concurrent.futures.ProcessPoolExecutor(max_workers=cpu_cores) as executor:
            executor.map(self._process_chunk, chunks)

    def _process_chunk(self, dates_chunk):
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        session = requests.Session()

        for issuer, last_date in dates_chunk.items():
            if isinstance(last_date, str):
                last_date = datetime.strptime(last_date, "%Y-%m-%d").date()

            if last_date.date() == datetime.now().date():
                continue

            self._update_missing_data(issuer, last_date, cursor, session)
            conn.commit()  # Commit after each issuer

        cursor.close()
        conn.close()

    def _update_missing_data(self, issuer, last_date, cursor, session):
        cursor.execute("SELECT issuer_id FROM issuers WHERE code = %s", (issuer,))
        issuer_id = cursor.fetchone()[0]

        years = self._calculate_years_needed(last_date)
        print(f"Fetching {years} year(s) of updates for {issuer}")

        for year in range(years):
            url = self._build_update_url(issuer, year, last_date)
            try:
                soup = self._get_soup(session, url)
                data = self._parse_soup_data(soup)

                if data:
                    self._store_new_data(issuer_id, data, last_date, cursor)
            except Exception as e:
                print(f"Error updating {issuer} year {year}: {e}")

    def _calculate_years_needed(self, last_date):
        delta = datetime.now().date() - last_date
        return max(1, (delta.days // 364) + 1)

    def _build_update_url(self, issuer, year_offset, last_date):
        days = 364
        now = datetime.now()
        to_date = now - timedelta(days=days * year_offset)
        from_date = to_date - timedelta(days=days)

        if from_date < last_date:
            from_date = last_date + timedelta(days=1)

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

    def _store_new_data(self, issuer_id, data, last_date, cursor):
        rows = []
        for row in data:
            try:
                row_date = self._parse_date(row[0])
                if row_date <= last_date:
                    continue

                rows.append((
                    issuer_id,
                    row_date,
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

        if rows:
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
        return float(num_str.replace('.', '').replace(',', '.'))

    def _parse_percent(self, percent_str):
        return float(percent_str.replace('%', '').replace(',', '.'))

    def _parse_int(self, int_str):
        return int(int_str.replace('.', ''))

    def _split_dict(self, input_dict, n):
        items = list(input_dict.items())
        split_size = len(items) // n
        remainder = len(items) % n

        splits = []
        start = 0
        for i in range(n):
            end = start + split_size + (1 if i < remainder else 0)
            splits.append(dict(items[start:end]))
            start = end

        return splits
