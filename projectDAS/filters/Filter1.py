from projectDAS.filters.Filter import *

class CodeDownloaderFilter(Filter):
    async def process(self, driver, date):
        print("Filter 1 starting...")

        code_dropdown = driver.find_element(By.ID, 'Code')
        option_list = code_dropdown.find_elements(By.TAG_NAME, 'option')
        for option in option_list:
            if not re.search(r'\d', option.text):
                date.append(option.text)

        return driver, date[:5]

if __name__ == '__main__':

    print("Filter 1 starting...")
    url = "https://www.mse.mk/mk/stats/symbolhistory/kmb"