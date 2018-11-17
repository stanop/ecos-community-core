package ru.citeck.ecos.webscripts.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.menu.MenuService;
import ru.citeck.ecos.menu.dto.Menu;

import java.io.IOException;
import java.io.OutputStream;

public class MenuGet extends AbstractWebScript {

    private MenuService menuService;

    private static final String PARAM_USERNAME = "userName";
    private ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
        String userName = req.getParameter(PARAM_USERNAME);
        Menu menuConfig;

        try {
            if (userName == null) {
                menuConfig = menuService.queryMenu();
            } else {
                menuConfig = menuService.queryMenu(userName);
            }
        } catch (RuntimeException re) {
            throw new WebScriptException(Status.STATUS_NO_CONTENT, "Error getting menu data.", re);
        }

        resp.setContentType("application/json");
        resp.setContentEncoding("UTF-8");

        try (OutputStream os = resp.getOutputStream()) {
            objectMapper.writeValue(os, menuConfig);
        } catch (RuntimeException re) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error writing JSON response.", re);
        }
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
