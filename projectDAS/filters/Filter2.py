import os.path
import time

from projectDAS.filters.Filter import *

class SaveDataFilter(Filter):
    async def process(self, driver, data):
        print("Filter 2 starting...")
        dates = {}
        for company in data:
            print(company)
            await change_company_code(driver, company)
            await change_input_values(driver, datetime.now())

            if check_existing_data(company):
                print('Getting last date')
                dates[company] = await self.get_last_date(company)
                print(dates[company])
            else:
                dates[company] = transform_date_to_string(datetime.now())
                await self.save_last_10_years(driver, company)
        print(len(dates), len(data))

        return driver, dates

    async def get_last_date(self, company_name):
        path = os.path.join('..', 'database', f'{company_name}.xlsx')
        df = pd.read_excel(path)
        return df.iloc[0, 0]

    async def save_last_10_years(self, driver, company_name):
        for i in range(10):
            print(i, company_name)

            await self.change_date(driver, i)
            time.sleep(3)
            df = await get_df(driver)

            output_dir = os.path.join('..', 'database', f'{company_name}.xlsx')
            if not os.path.exists(output_dir):
                df.to_excel(output_dir, index=False)
            else:
                read = pd.read_excel(output_dir)
                combined = pd.concat([read, df], ignore_index=True) # read e prvo pa posle df
                combined.to_excel(output_dir, index=False)

    async def change_date(self, driver, i):
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

        print(date_Od, date_Do)
        time.sleep(3)

        await click_button(driver)

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