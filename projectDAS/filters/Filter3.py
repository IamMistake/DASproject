from projectDAS.filters.Filter import *

class DataCompletenessFilter(Filter):
    async def process(self, driver, dates):
        print("Filter 3 starting...")
        for k, v in dates.items():
            last_date = transform_string_to_date(v)
            if last_date == datetime.now():
                continue

            df = await self.get_new_data(driver, k, last_date)

            output_dir = os.path.join('..', 'database', f'{k}.xlsx')
            read = pd.read_excel(output_dir)
            combined = pd.concat([df, read], ignore_index=True) # df e prvo pa posle read
            combined.to_excel(output_dir, index=False)

        return driver, dates

    async def get_new_data(self, driver, company, last_date):
        await change_company_code(driver, company)

        await change_input_values(driver, last_date)

        await click_button(driver)

        df = await get_df(driver)
        return df

