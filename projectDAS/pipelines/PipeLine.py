from openpyxl.worksheet.filters import Filters
import asyncio

# from projectDAS.filters.Filter import *
from projectDAS.filters.Filter1 import *
from projectDAS.filters.Filter2 import *
from projectDAS.filters.Filter3 import *

class ScrapingPipeline:
    def __init__(self):
        self.filters = []
        self.filters.append(CodeDownloaderFilter())
        self.filters.append(SaveDataFilter())
        # self.filters.append(DataCompletenessFilter())

    def add_filter(self, filter):
        self.filters.append(filter)

    async def execute(self, driver, data):
        for filter in self.filters:
            driver, data = await filter.process(driver, data)
        return data

if __name__ == '__main__':

    print("ScrapingPipeline starting...")
    timer1 = time.time()
    url = 'https://www.mse.mk/mk/stats/symbolhistory/kmb'
    # download_dir = os.path.join(os.getcwd(), 'projectDAS', 'database')
    download_dir = os.path.join('..', 'database')

    chrome_options = Options()
    chrome_options.add_argument("--start-maximized")
    chrome_options.add_experimental_option("prefs", {
        "download.default_directory": download_dir,
        "download.prompt_for_download": False,
        "download.directory_upgrade": True,
        "safebrowsing.enabled": True
    })

    pipeline = ScrapingPipeline()

    # TODO REMOVE/FIX FILTER 1 RETURN []
    driver = webdriver.Chrome(service=Service(), options=chrome_options)
    try:
        driver.get(url)

        time.sleep(2)
        timer2 = time.time()
        filtered_data = asyncio.run(pipeline.execute(driver, []))

    finally:
        driver.quit()

    duration = time.time() - timer1
    print("Whole app finished in {:.2f} seconds".format(duration))
    duration = time.time() - timer2
    print("Pipeline finished in {:.2f} seconds".format(duration))
    print("ScrapingPipeline finished!!!")
    # print(filtered_data)