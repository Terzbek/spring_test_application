package ru.codekitchen.controller.secured;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.codekitchen.entity.RecordStatus;
import ru.codekitchen.entity.User;
import ru.codekitchen.entity.dto.RecordsContainerDto;
import ru.codekitchen.service.RecordService;
import ru.codekitchen.service.UserService;

@Controller
@RequestMapping("/account")
public class PrivateAccountController {
    private final UserService userService;
    private final RecordService recordService;

    @Autowired
    public PrivateAccountController(UserService userService, RecordService recordService) {
        this.userService = userService;
        this.recordService = recordService;
    }

    @GetMapping
    public String getMainPage(Model model, @RequestParam(name = "filter", required = false) String filterMode) {
        User user = userService.getCurrentUser();
        RecordsContainerDto container = recordService.findAllRecords(filterMode);
        model.addAttribute("userName", user.getName());
        model.addAttribute("records", container.getRecords());
        model.addAttribute("numberOfDoneRecords", container.getNumberOfDoneRecords());
        model.addAttribute("numberOfActiveRecords", container.getNumberOfActiveRecords());
        return "private/account-page";
    }

    @PostMapping(value = "add-record")
    public String addRecord(@RequestParam String title) {
        recordService.saveRecord(title);
        return "redirect:/account";
    }

    @RequestMapping(value = "/make-record-done", method = RequestMethod.POST)
    public String makeRecordDone(@RequestParam Long id,
                                 @RequestParam(name = "filter", required = false) String filterMode) {
        recordService.updateRecordStatus(id, RecordStatus.DONE);
        return "redirect:/account" + (filterMode != null && !filterMode.isBlank() ? "?filter=" + filterMode : "");
    }

    @RequestMapping(value = "/delete-record", method = RequestMethod.POST)
    public String deleteRecord(@RequestParam Long id,
                               @RequestParam(name = "filter", required = false) String filterMode) {
        recordService.deleteRecord(id);
        return "redirect:/account" + (filterMode != null && !filterMode.isBlank() ? "?filter=" + filterMode : "");
    }
}
