import pandas as pd
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
import time
from datetime import datetime, timedelta
import os
from datetime import datetime
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import re


def mnozi(broj):
    return float(broj) / 100.0


def transform_string_to_date(stringce):
    date_format = "%d.%m.%Y"
    return datetime.strptime(stringce, date_format)


def deli(broj):
    br = str(broj)
    # print(broj, br)
    if br.endswith('.0'):
        return int(broj)
    else:
        return int(br.replace(".", ""))


def transform_date_to_string(date):
    tmp = date.__str__().split(' ')[0].split("-")
    return tmp[2] + "." + tmp[1] + "." + tmp[0]


def check_existing_data(company_name):
    path = os.path.join('..', 'database', f'{company_name}.xlsx')
    if os.path.exists(path):
        return True
    return False


async def get_df(driver):
    html = driver.page_source
    soup = BeautifulSoup(html, 'html.parser')
    table = soup.select_one('#resultsTable')
    trs = table.select('tbody > tr')

    df = {
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

    for tr in trs:
        tds = tr.select('td')

        df['Датум'].append(tds[0].text)
        df['Цена на последна трансакција'].append(tds[1].text)
        df['Мак.'].append(tds[2].text)
        df['Мин.'].append(tds[3].text)
        df['Просечна цена'].append(tds[4].text)
        df['%пром.'].append(tds[5].text)
        df['Количина'].append(tds[6].text)
        df['Промет во БЕСТ во денари'].append(tds[7].text)
        df['Вкупен промет во денари'].append(tds[8].text)

    tmp = pd.DataFrame(df)
    return tmp


async def click_button(driver):
    btn = driver.find_element(By.CLASS_NAME, 'btn-primary-sm')
    btn.click()


async def change_input_values(driver, last_date):
    input_Od = driver.find_element(By.ID, 'FromDate')
    input_Do = driver.find_element(By.ID, 'ToDate')

    tmp1 = transform_date_to_string(datetime.now())
    input_Do.clear()
    input_Do.send_keys(tmp1)

    date = last_date + timedelta(days=1)
    tmp2 = transform_date_to_string(date)
    input_Od.clear()
    input_Od.send_keys(tmp2)


async def change_company_code(driver, company):
    code_dropdown = Select(driver.find_element(By.ID, 'Code'))
    code_dropdown.select_by_value(company)

    input_Do = driver.find_element(By.ID, 'ToDate')
    input_Do.clear()
    input_Do.send_keys(transform_date_to_string(datetime.now()))

    # btn = driver.find_element(By.CLASS_NAME, 'btn-primary-sm')
    # btn.click()


class Filter:
    async def process(self, driver, data):
        raise NotImplementedError("Each filter must implement!")
