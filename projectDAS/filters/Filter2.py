import os.path

import pandas as pd

from projectDAS.filters.Filter import *

class SaveDataFilter(Filter):
    def process(self, data):
        print("Filter 2 starting...")
        num_threads = 3
        chunk_size = ceil(len(data) / num_threads)
        data_chunks = [data[i:i + chunk_size] for i in range(0, len(data), chunk_size)]

        all_dates = {}
        with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
            # Submit each data chunk to a separate thread
            futures = [executor.submit(self.process_companies_subset, chunk) for chunk in data_chunks]

            # Collect results as they complete
            for future in concurrent.futures.as_completed(futures):
                all_dates.update(future.result())
                print(all_dates)

        return all_dates

    def process_companies_subset(self, companies_subset):
        dates = {}
        driver = get_driver()
        print(companies_subset)
        for company in companies_subset:
            change_company_code(driver, company)
            change_input_values(driver, datetime.now())

            if check_existing_data(company):
                dates[company] = self.get_last_date(company)
            else:
                dates[company] = transform_date_to_string(datetime.now())
                self.save_last_10_years(driver, company)

        driver.quit()
        return dates

    def get_last_date(self, company_name):
        path = os.path.join('..', 'database', f'{company_name}.xlsx')
        df = pd.read_excel(path)
        return df.iloc[0, 0]

    def save_last_10_years(self, driver, company_name):
        output_dir = os.path.join('..', 'database', f'{company_name}.xlsx')
        tmp = {
            "Датум": [],
            "Цена на последна трансакција": [],
            "Мак.": [],
            "Мин.": [],
            "Просечна цена": [],
            "%пром.": [],
            "Количина": [],
            "Промет во БЕСТ во денари": [],
            "Вкупен промет во денари": [],
        }
        excelce = pd.DataFrame(tmp)
        for i in range(10):
            print(i, company_name)

            self.change_date(driver, i)
            time.sleep(3)
            df = get_df(driver)
            if df is None:
                print("Found None in " + i + " " + company_name)
                break

            excelce = pd.concat([excelce, df], ignore_index=True)

        excelce.to_excel(output_dir, index=False)

    def change_date(self, driver, i):
        input_Od = driver.find_element(By.ID, 'FromDate')
        input_Do = driver.find_element(By.ID, 'ToDate')
        input_Do_value = input_Od.get_attribute('value')

        date_Do = transform_string_to_date(input_Do_value)

        days = 363
        if i != 0:
            date_Do = transform_string_to_date(input_Od.get_attribute('value'))
            date_Do = date_Do - timedelta(days=1)

            tmp = transform_date_to_string(date_Do)
            input_Do.clear()
            input_Do.send_keys(tmp)

        date_Od = date_Do - timedelta(days=days)

        prt = transform_date_to_string(date_Od)
        input_Od.clear()
        input_Od.send_keys(prt)

        # print(date_Od, date_Do)
        time.sleep(3)

        click_button(driver)

    def html_df(self):
        html = pd.read_html('projectDAS/database/Историски податоци.xls')
        html = html[0]

        html['%пром.'] = html['%пром.'].astype('float64')
        html.iloc[0:, 5] = html.iloc[0:, 5].apply(mnozi)
        html.iloc[0:, 6] = html.iloc[0:, 6].apply(deli)
        html['Количина'] = html['Количина'].astype('int64')
        return html

if __name__ == '__main__':

    print("Filter 2 starting...")
    url = "https://www.mse.mk/mk/stats/symbolhistory/kmb"


    # TODO funkcija koja dostapuva do ./database/f{kod od companija}.xlsx
    # TODO ----- Postoi
    # TODO ako postoi se proveruva posleniot datum (prviot red) dali e postar ili ednakov so Do_dropdown datum
    # TODO ako e pomal se zemaat site podatoci izmejgu tie datumi i se stavaat najgore od excelot
    # TODO ako e pogolem ili ednakov nisto
    # TODO ----- Ne postoi
    # TODO se kreira file vo ./database/f{kod od companija}.xlsx
    # TODO i se simnuvaat poslednite 10 godini od denesniot datum (mora godina po godina)
    # TODO i posledniot datum se prenesuva na Filter 3