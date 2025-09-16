from datetime import datetime
from pipelines.PipeLine import ScrapingPipeline
from config import DB_CONFIG
import psycopg2

def create_database_tables():

    """Create necessary tables in PostgreSQL if they don't exist"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS issuers (
        issuer_id SERIAL PRIMARY KEY,
        code VARCHAR(20) UNIQUE NOT NULL,
        name VARCHAR(255) NOT NULL,
        last_updated TIMESTAMP
    )
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS stock_data (
        data_id SERIAL PRIMARY KEY,
        issuer_id INTEGER REFERENCES issuers(issuer_id),
        date DATE NOT NULL,
        last_price NUMERIC(12, 2),
        max_price NUMERIC(12, 2),
        min_price NUMERIC(12, 2),
        avg_price NUMERIC(12, 2),
        percent_change NUMERIC(6, 2),
        quantity INTEGER,
        best_turnover NUMERIC(15, 2),
        total_turnover NUMERIC(15, 2),
        CONSTRAINT unique_issuer_date UNIQUE (issuer_id, date)
    )
    """)

    conn.commit()
    cursor.close()
    conn.close()

if __name__ == '__main__':

    print("ScrapingPipeline starting...")
    create_database_tables()

    timer1 = datetime.now().time()
    url = 'https://www.mse.mk/mk/stats/symbolhistory/kmb'

    pipeline = ScrapingPipeline()
    filtered_data = pipeline.execute([])

    duration = datetime.now().time()
    print("Whole app finished in {:.2f} seconds".format(duration))
    print("ScrapingPipeline finished!!!")
