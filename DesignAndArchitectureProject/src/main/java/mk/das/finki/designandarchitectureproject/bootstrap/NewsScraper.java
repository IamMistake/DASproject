package mk.das.finki.designandarchitectureproject.bootstrap;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NewsScraper {

    public static Map<String, String> companies = new HashMap<>();

    @PostConstruct
    public void init() {
        companies = new HashMap<>();

        companies.put("Алкалоид АД Скопје", "ALK");
        companies.put("Гранит АД Скопје", "GRT");
        companies.put("Комерцијална Банка АД Скопје", "KMB");
        companies.put("Македонијатурист АД Скопје", "MATU");
        companies.put("Макпетрол АД Скопје", "MPT");
        companies.put("Макстил АД Скопје", "MST");
        companies.put("НЛБ Банка АД Скопје", "NLB");
        companies.put("Стопанска банка АД Скопје", "STOB");
        companies.put("ТТК Банка АД Скопје", "TTK");
        companies.put("УНИБанка АД Скопје", "UNIB");
        companies.put("Стопанска банка АД Битола", "STOB");
        companies.put("ОКТА АД Скопје", "OKTA");
        companies.put("ВВ Тиквеш АД Скопје", "VVT");
        companies.put("Витаминка АД Прилеп", "VIT");
        companies.put("ЗК Пелагонија АД Битола", "ZKP");
        companies.put("Либерти АД Скопје", "LIB");
        companies.put("Македонски Телеком АД – Скопје", "MTK");
        companies.put("осигуруване Македонија АД Скопје ^ виена иншуренс груп", "OSIG");
        companies.put("Прилепска пиварница АД Прилеп", "PIPA");
        companies.put("Реплек АД Скопје", "REPL");
        companies.put("Тетекс АД Тетово", "TETT");
        companies.put("Тутунски комбинат АД Прилеп", "TUK");
        companies.put("Фершпед АД Скопје", "FERS");
        companies.put("Хотели-Метропол Охрид", "HOTM");
        companies.put("Цементарница УСЈЕ АД Скопје", "CEMA");
        companies.put("Адинг АД * Скопје", "ADNG");
    }
}
