package ru.citeck.ecos.webscripts.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.menu.MenuService;
import ru.citeck.ecos.menu.dto.Menu;

import java.io.IOException;

public class MenuGet extends AbstractWebScript {

    private MenuService menuService;

    private static final String PARAM_USERNAME = "userName";
    private ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String userName = req.getParameter(PARAM_USERNAME);
        Menu menuConfig;
        if (userName == null) {
            menuConfig = menuService.queryMenu();
        } else {
            menuConfig = menuService.queryMenu(userName);
        }
        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), menuConfig);
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
