from filters.Filter import Filter
from bs4 import BeautifulSoup
import requests
import re
from config import DB_CONFIG
import psycopg2

class CodeDownloaderFilter(Filter):

    def process(self, data):
        print("Filter 1: Downloading issuer codes from MSE...")

        url = 'https://www.mse.mk/mk/stats/symbolhistory/kmb'
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            html = BeautifulSoup(response.text, 'html.parser')

            dropdown = html.select_one('#Code')
            if not dropdown:
                raise ValueError("Could not find issuer dropdown on MSE page")

            options_list = dropdown.select('option')
            issuers = []

            for option in options_list:
                if option.text and not re.search(r'\d', option.text):
                    issuers.append(option.text)

            self._store_issuers(issuers)
            return issuers[:25]  # Limit to 25 as in original

        except Exception as e:
            print(f"Error in CodeDownloaderFilter: {e}")
            raise

    def _store_issuers(self, issuers):
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        for issuer in issuers:
            try:
                cursor.execute("""
                    INSERT INTO issuers (code, name) 
                    VALUES (%s, %s)
                    ON CONFLICT (code) DO NOTHING
                """, (issuer, issuer))
            except Exception as e:
                print(f"Error storing issuer {issuer}: {e}")

        conn.commit()
        cursor.close()
        conn.close()
