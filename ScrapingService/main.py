import csv
import os
import pandas as pd
def split_dict(input_dict, n):
    items = list(input_dict.items())
    split_size = len(items) // n
    remainder = len(items) % n

    splits = []
    start = 0
    for i in range(n):
        end = start + split_size + (1 if i < remainder else 0)
        splits.append(dict(items[start:end]))
        start = end

    return splits
if __name__ == '__main__':
    tmp = {'ADIN': '13.11.2024', 'ALK': '13.11.2024', 'ALKB': '13.11.2024', 'AMBR': '13.11.2024', 'AMEH': '13.11.2024', 'APTK': '13.11.2024', 'ATPP': '13.11.2024'}

    splits = split_dict(tmp, 3)
    print(splits)