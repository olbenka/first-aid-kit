package ru.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.project.dao.AppUserDAO;
import ru.project.dao.MedicationDAO;
import ru.project.dao.RawDataDAO;
import ru.project.entity.*;
import ru.project.entity.enums.UserState;
import ru.project.exeptions.UploadFileException;
import ru.project.service.*;
import ru.project.service.enums.LinkType;
import ru.project.service.enums.ServiceCommand;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static ru.project.entity.enums.UserState.*;
import static ru.project.service.enums.ServiceCommand.*;

@Service
@Log4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final AppUserService appUserService;
    private final MedicationDAO medicationDAO;

    private String medicationName = "";
    private Integer medicationQuantity = 0;
    private LocalDate expiryMedDate;
    private String briefInfo = "";
    private AppPhoto appPhoto = null;
    private AppDocument appDocument = null;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String text = update.getMessage().getText();
        String output = "";
        Long chatId = update.getMessage().getChatId();

        ServiceCommand serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, serviceCommand);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
        } else if (WAIT_FOR_NAME_STATE.equals(userState)) {
            if (appPhoto != null) {
                appPhoto = null;
            }
            if (appDocument != null) {
                appDocument = null;
            }
            output = processNameInput(appUser, text);
        } else if (WAIT_FOR_QUANTITY_STATE.equals(userState)) {
            output = processQuantityInput(appUser, text);
        } else if (WAIT_FOR_DATE_STATE.equals(userState)) {
            output = processDateInput(appUser, text);
        } else if (WAIT_FOR_INFO_STATE.equals(userState)) {
            output = processInfoInput(appUser, text);
        } else if (WAIT_FOR_NAME_TO_DELETE_STATE.equals(userState)) {
            output = processDelete(appUser, text);
        } else if (WAIT_FOR_NAME_TO_TAKE_STATE.equals(userState)){
            output = processTakeCommand(appUser,text);
        }  else if (WAIT_FOR_SHOW_STATE.equals(userState)) {
            output = processInfoShow(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка. Введите /cancel и попробуйте снова.";
        }

        sendAnswer(output, chatId);
    }

    private String processServiceCommand(AppUser appUser, ServiceCommand serviceCommand) {
        if (REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);
        } else if (START.equals(serviceCommand)) {
            return "Приветствую, " + appUser.getFirstName() +
                    "\nДанный бот является виртуальной аптечкой. Здесь вы можете хранить данные о ваших медикаментах," +
                    "следить за сроками годности," +
                    "отмечать прием таблеток," +
                    "следить за количеством." +
                    "\nЧтобы посмотреть список доступных команд, нажмите меню в левой части экрана.";
        } else if (ADD.equals(serviceCommand)) {
            appUser.setUserState(WAIT_FOR_NAME_STATE);
            appUserDAO.save(appUser);
            return "Процесс добавления медикамента. \n" +
                    "Введите название: \n";
        } else if (ALLINFO.equals(serviceCommand)) {
            return getInfo(appUser);
        } else if (INFO.equals(serviceCommand)) {
            appUser.setUserState(WAIT_FOR_SHOW_STATE);
            appUserDAO.save(appUser);
            return "Информация о конкретном медикаменте. \n" +
                    "Введите название и номер требуемых данных через запятую: \n" +
                    "1. Количество \n" +
                    "2. Срок годности \n" +
                    "3. Краткая информация \n" +
                    "4. Фото (отправляется в виде ссылки для скачивания)\n";
        } else if (DELETE.equals(serviceCommand)) {
            appUser.setUserState(WAIT_FOR_NAME_TO_DELETE_STATE);
            appUserDAO.save(appUser);
            return "Процесс удаления медикамента. \n" +
                    "Введите название: \n";
        } else if (TAKE.equals(serviceCommand)) {
            appUser.setUserState(WAIT_FOR_NAME_TO_TAKE_STATE);
            appUserDAO.save(appUser);
            return "Процесс принятия медикамента. \n" +
                    "Введите название и количество через пробел: \n";
        } else if (SHOW_EXP_SOON.equals(serviceCommand)){
            return getExpInfo(appUser);
        } else {
            return "Неизвестная команда!";
        }
    }
    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            if (appDocument == null && appPhoto == null) {
                appDocument = fileService.processDoc(update.getMessage());
                saveMedication(appUser);
                sendAnswer("Документ успешно загружен! Медикамент сохранен.", chatId);
            } else {
                sendAnswer("Уже есть документ или фотография.", chatId);
            }
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка не удалась. Повторите попытку позже";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            if (appDocument == null && appPhoto == null) {
                appPhoto = fileService.processPhoto(update.getMessage());
                saveMedication(appUser);
                sendAnswer("Фото успешно загружено! Медикамент сохранен.", chatId);
            } else {
                sendAnswer("Уже есть документ или фотография.", chatId);
            }

        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка не удалась. Повторите попытку позже";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            String error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }


    private String getInfo(AppUser appUser) {
        List<Medication> medications = medicationDAO.findByUser(appUser);
        if (medications.isEmpty()) {
            return "У вас нет зарегистрированных медикаментов.";
        } else {
            StringBuilder info = new StringBuilder("Информация о ваших медикаментах:\n\n");
            for (Medication medication : medications) {
                info.append("Название: ").append(medication.getName())
                        .append(", Количество: ").append(medication.getQuantity())
                        .append("\n");
            }
            return info.toString();
        }
    }

    private String getExpInfo(AppUser appUser) {
        List<Medication> medications = medicationDAO.findByUser(appUser);
        if (medications.isEmpty()) {
            return "У вас нет зарегистрированных медикаментов.";
        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM.yyyy");
            boolean tmp = false;
            StringBuilder info = new StringBuilder("Информация о ваших медикаментах:\n\n");
            for (Medication medication : medications) {
                LocalDate currentDate = LocalDate.now();
                if (currentDate.plusMonths(1).isAfter(medication.getExpiryDate())) {
                    tmp = true;
                    String formattedExpiryDate = medication.getExpiryDate().format(dateFormatter);
                    info.append("Название: ").append(medication.getName()).append("\n")
                            .append("Срок годности: ").append(formattedExpiryDate).append("\n\n");
                }
            }
            if (tmp) {
                return info.toString();
            }
            return "У вас нет медикаментов, которые скоро испортятся.";
        }
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена.";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramBot = update.getMessage().getFrom();
        var optional = appUserDAO.findByTelegramUserId(telegramBot.getId());
        if (optional.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramBot.getId())
                    .userName(telegramBot.getUserName())
                    .firstName(telegramBot.getFirstName())
                    .isActive(false)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }
    private void saveMedication(AppUser appUser) {
        Medication medication = Medication.builder()
                .name(medicationName)
                .quantity(medicationQuantity)
                .expiryDate(expiryMedDate)
                .briefInfo(briefInfo)
                .user(appUser)
                .photo(appPhoto)
                .document(appDocument)
                .build();
        medicationDAO.save(medication);
    }
    private void saveRawData(Update update) {
        var rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private String processInfoShow(AppUser appUser, String text) {
        String[] inputParts = text.split(",");

        if (inputParts.length == 2) {
            String medicationName = inputParts[0].trim();
            int requestedDataNumber = Integer.parseInt(inputParts[1].trim());

            var optionalMedication = medicationDAO.findMedicationByNameAndUser(medicationName, appUser);

            if (optionalMedication.isPresent()) {
                Medication medication = optionalMedication.get();
                appUser.setUserState(BASIC_STATE);
                appUserDAO.save(appUser);
                switch (requestedDataNumber) {
                    case 1:
                        return "Количество медикамента " + medication.getName() + ": " + medication.getQuantity();
                    case 2:
                        return "Срок годности медикамента " + medication.getName() + ": " + medication.getExpiryDate();
                    case 3:
                        return "Краткая информация о медикаменте " + medication.getName() + ": " + medication.getBriefInfo();
                    case 4:
                        if (medication.getPhoto() == null && medication.getDocument() == null) {
                            return "Вы не добавляли фото медикамента. Введите данные еще раз или используйте команду /cancel.";
                        } else if (medication.getDocument() == null) {
                            return "Фото медикамента " + medication.getName() + ": " +
                                    fileService.generateLink(medication.getPhoto().getId(), LinkType.GET_PHOTO);
                        }
                        return "Фото медикамента " + medication.getName() + ": " +
                                fileService.generateLink(medication.getDocument().getId(), LinkType.GET_DOC);
                    default:
                        return "Некорректный номер данных. Введите номер от 1 до 4.";
                }
            } else {
                return "Медикамент с указанным названием не найден. Введите данные еще раз или используйте команду /cancel.";
            }
        } else {
            return "Некорректный ввод. Введите название медикамента и номер требуемых данных через запятую.";
        }
    }

    private String processDelete(AppUser appUser, String text) {
        var optionalMedication = medicationDAO.findMedicationByNameAndUser(text, appUser);

        if (optionalMedication.isPresent()) {
            Medication medicationToDelete = optionalMedication.get();
            medicationDAO.delete(medicationToDelete);
            appUser.setUserState(BASIC_STATE);
            appUserDAO.save(appUser);
            return "Медикамент " + text + " успешно удален.";
        } else {
            return "Медикамент с названием " + text + " не найден. Введите данные еще раз или используйте команду /cancel.";
        }
    }

    private String processTakeCommand(AppUser appUser, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) {
            return "Ошибка! Введите название и количество через пробел.";
        }

        String medicationName = parts[0];
        int quantityTaken;
        try {
            quantityTaken = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "Ошибка! Введите корректное количество.";
        }

        var optionalMedication = medicationDAO.findMedicationByNameAndUser(medicationName, appUser);

        if (optionalMedication.isPresent()) {
            Medication medication = optionalMedication.get();
            int remainingQuantity = medication.getQuantity() - quantityTaken;

            if (remainingQuantity > 0) {
                medication.setQuantity(remainingQuantity);
                medicationDAO.save(medication);
                appUser.setUserState(BASIC_STATE);
                appUserDAO.save(appUser);
                return "Медикамент " + medicationName + " успешно принят. Остаток: " + remainingQuantity;
            } else if (remainingQuantity == 0){
                medicationDAO.delete(medication);
                appUser.setUserState(BASIC_STATE);
                appUserDAO.save(appUser);
                return "Медикамент " + medicationName + " успешно принят.\n\n" +
                        "ВНИМАНИЕ: медикамент закончился. Он был автоматически удален из списка.";
            } else {
                return "Недостаточное количество медикамента " + medicationName + ". Введите данные еще раз или используйте команду /cancel.";
            }
        } else {
            return "Медикамент с названием " + medicationName + " не найден. Введите данные еще раз или используйте команду /cancel.";
        }
    }


    private String processDateInput(AppUser appUser, String text) {
        try {
            String warning = "";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.yyyy");
            YearMonth expiryYearMonth = YearMonth.parse(text, formatter);

            LocalDate currentDate = LocalDate.now();
            LocalDate expiryDate = expiryYearMonth.atEndOfMonth();

            if (expiryYearMonth.isBefore(YearMonth.now())) {
                return "Ошибка! Ваш срок годности вышел. Введите дату, которая идет после текущей даты.";
            } else if (currentDate.plusMonths(1).isAfter(expiryDate)) {
                // Если остался месяц или меньше до конца срока годности
                warning = "ВНИМАНИЕ! До конца срока годности остался месяц или меньше. Ваш медикамент добавлен, но пожалуйста, будьте внимательны.";
            }

            expiryMedDate = expiryYearMonth.atDay(1);
            appUser.setUserState(WAIT_FOR_INFO_STATE);
            appUserDAO.save(appUser);
            warning += "\nВведите краткую информацию:";
            return warning;
        } catch (DateTimeParseException e) {
            return "Ошибка! Введите корректную дату в формате MM.YYYY.";
        }
    }

    private String processInfoInput(AppUser appUser, String text) {
        briefInfo = text;
        appUser.setUserState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Бот поддерживает хранение фотографии медикамента, " +
                "что аналогично форме выпуска из стандартных инструкций по применению. \n" +
                "Можно загрузить одну фотографию в сжатом или в исходном виде.";
    }

    private String processQuantityInput(AppUser appUser, String text) {
        try {
            medicationQuantity = Integer.parseInt(text);
            appUser.setUserState(WAIT_FOR_DATE_STATE);
            appUserDAO.save(appUser);
            return "Введите срок годности в формате MM.YYYY:";
        } catch (NumberFormatException e) {
            return "Ошибка! Введите корректное количество.";
        }
    }

    private String processNameInput(AppUser appUser, String text) {
        var optional = medicationDAO.findMedicationByName(text);

        if (optional.isEmpty()) {
            medicationName = text;
            appUser.setUserState(WAIT_FOR_QUANTITY_STATE);
            appUserDAO.save(appUser);
            return "Введите количество: ";
        } else {
            return "Этот медикамент уже есть в вашей аптечке. Введите корректные данные еще раз или используйте команду /cancel.";
        }
    }


}
