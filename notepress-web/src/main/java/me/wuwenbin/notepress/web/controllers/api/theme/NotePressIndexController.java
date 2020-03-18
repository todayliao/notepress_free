package me.wuwenbin.notepress.web.controllers.api.theme;

import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author wuwenbin
 */
@Controller
@RequestMapping()
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressIndexController extends NotePressBaseController {

    @GetMapping("/")
    public ModelAndView indexPage() {
        return new ModelAndView("index");
    }
}
