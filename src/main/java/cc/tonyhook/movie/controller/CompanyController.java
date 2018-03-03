package cc.tonyhook.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.domain.Company;
import cc.tonyhook.movie.domain.CompanyRepository;

@RestController
public class CompanyController {
    @Autowired
    private CompanyRepository companyRepository;

    @RequestMapping(value = "/movie/company/list", method = RequestMethod.GET)
    public @ResponseBody Iterable<Company> getCompanies() {
        Iterable<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            company.setMovie_companies(null);
        }

        return companies;
    }

}
