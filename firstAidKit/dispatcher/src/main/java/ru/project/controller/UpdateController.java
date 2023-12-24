package ru.project.controller;

import com.rabbitmq.client.Command;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.project.service.UpdateProducer;
import ru.project.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.project.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать"));
        listOfCommands.add(new BotCommand("/cancel", "Отменить текущую команду"));
        listOfCommands.add(new BotCommand("/registration", "Регистрация нового пользователя"));
        listOfCommands.add(new BotCommand("/add", "Добавить медикамент"));
        listOfCommands.add(new BotCommand("/allinfo", "Список всех медикаментов с остатком"));
        listOfCommands.add(new BotCommand("/info", "Информация о конкретном препарате"));
        listOfCommands.add(new BotCommand("/delete", "Удаление медикамента"));
        listOfCommands.add(new BotCommand("/take", "Принять медикамент"));
        listOfCommands.add(new BotCommand("/showexpiringsoon", "Показать медикаменты, срок годности которых заканчивается через месяц или меньше"));

        try {
            telegramBot.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }

        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported message type is received " + update);
        }
    }

    // по очередям брокера
    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setFileReceivedView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageText(update,
                "Файл получен. Обработка...");
        setView(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageText(update,
                "Неподдерживаемый тип сообщений.");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendMessage(sendMessage);
    }


}
