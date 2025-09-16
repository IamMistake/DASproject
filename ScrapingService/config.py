import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'postgres'),
    'database': os.getenv('DB_NAME', 'mse_stock_data'),
    'user': os.getenv('DB_USER', 'dians'),
    'password': os.getenv('DB_PASSWORD', 'dians123'),
    'port': os.getenv('DB_PORT', '5432')  # todo change the port
}
