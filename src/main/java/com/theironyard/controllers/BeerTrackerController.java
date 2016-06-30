package com.theironyard.controllers;

import com.theironyard.PasswordStorage;
import com.theironyard.entities.Beer;
import com.theironyard.entities.User;
import com.theironyard.services.BeerRepository;
import com.theironyard.services.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.h2.tools.Server;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

/**
 * Created by zach on 11/10/15.
 */
@Controller
public class BeerTrackerController {
    @Autowired
    BeerRepository beerRepo;

    @Autowired
    UserRepository userRepo;

    @PostConstruct
    public void init() throws  SQLException {
        Server.createWebServer().start();
    }

    @RequestMapping(path="/", method = RequestMethod.GET)
    public String home(HttpSession session, Model model, String type, Integer calories, String search) {
        String username = (String) session.getAttribute("username");
        User user = userRepo.findOneByName(username);

        if (user == null) {
            return "login";
        }

        if (search != null) {
            model.addAttribute("beerRepo", beerRepo.searchByName(search));
        } else if (type != null && calories != null) {
            model.addAttribute("beerRepo", beerRepo.findByTypeAndCaloriesIsLessThanEqual(type, calories));
        }
        else if (type != null) {
            model.addAttribute("beerRepo", beerRepo.findByTypeOrderByNameAsc(type));
        }
        else {
            model.addAttribute("beerRepo", beerRepo.findAll());
        }

        return "home";
    }

    @RequestMapping(path="/add-beer", method = RequestMethod.POST)
    public String addBeer(String beername, String beertype, int beercalories, HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in.");
        }

        User user = userRepo.findOneByName(username);

        Beer beer = new Beer();
        beer.name = beername;
        beer.type = beertype;
        beer.calories = beercalories;
        beer.user = user;
        beerRepo.save(beer);
        return "redirect:/";
    }

    @RequestMapping(path="/edit-beer", method = RequestMethod.PUT)
    public String editBeer(int id, String name, String type, HttpSession session) throws Exception {
        if (session.getAttribute("username") == null) {
            throw new Exception("Not logged in.");
        }
        Beer beer = beerRepo.findOne(id);
        beer.name = name;
        beer.type = type;
        beerRepo.save(beer);
        return "redirect:/";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, HttpSession session) throws Exception {

        User user = userRepo.findOneByName(username);

        if (user == null) {
            user = new User();
            user.name = username;
            user.password = PasswordStorage.createHash(password);
            userRepo.save(user);
        }
        else if (!PasswordStorage.verifyPassword(password, user.password )) {
            throw new Exception("Wrong password");
        }
        session.setAttribute("username", username);
        return "redirect:/";
    }

    @RequestMapping(path="/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
