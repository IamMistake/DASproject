from projectDAS.filters.Filter import *

class DataCompletenessFilter(Filter):
    def process(self, dates):
        print("Filter 3 starting...")

        driver = get_driver()

        for k, v in dates.items():
            last_date = transform_string_to_date(v)
            if last_date == datetime.now():
                continue

            df = self.get_new_data(driver, k, last_date)
            if df is None:
                continue

            output_dir = os.path.join('..', 'database', f'{k}.xlsx')
            read = pd.read_excel(output_dir)
            combined = pd.concat([df, read], ignore_index=True) # df e prvo pa posle read
            combined.to_excel(output_dir, index=False)

        driver.quit()
        return dates

    def get_new_data(self, driver, company, last_date):
        change_company_code(driver, company)

        change_input_values(driver, last_date)

        click_button(driver)

        df = get_df(driver)
        return df

