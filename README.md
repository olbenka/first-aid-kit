# first-aid-kit
# Описание
## Наименование 
Телеграм-бот аптечка.
## Предметная область
Медикаменты
## Краткое описание (будет отредактировано)
Бот является виртуальной аптечкой: удобно хранить данные в телефоне и отмечать то, что медикамент был принят, а еще отслеживать сроки годности, так как такая информация часто забывается (планируется добавить уведомление о том, что срок годности подходит к концу, если месяц срока годности отличается от текущего на 1, уведомление появляется 1 числа каждого месяца) 
## Описание функций 
Бот предоставляет следующие возможности: 
* регистрация пользователя \
Выполняется после вызова команды /registration. Требуется ввод email, происходит подтверждение регистрации. 
* добавление информации о медикаментах \
Выполняется после вызова команды /add.
Вводятся данные: название, количество, срок годности, краткая информация, одна фотография медикамента (может быть как в сжатом, так и в обычном виде)
* получение информации о медикаментах \
Выполняется после вызова команды /info.
Можно получить как информацию обо всех медикаментах с актуальным количеством, так и определенную информацию о конкретном медикаменте.
* удаление медикамента \
Выполняется после вызова команды /delete. Вводится название медикамента. 
* фиксирование приема медикамента \
Выполняется после вызова команды /take. Вводятся данные: название, количество принятого медикамента. При успешной операции выводится название и обновленное количество медикаментов. Иначе медикамент удаляется (при количестве = 0) или пользователь оповещается о том, что он не может принять медикамент, так как осталось меньше требуемого. 
# Данные
## Для каждого элемента данных - ограничения
### Medication 
* id (PK): уникальный идентификатор, генерируется автоматически.
* name: наименование медикамента (не должно быть пустым).
* quantity: количество медикамента (> 0, при значении 0 медикамент удаляется).
* expiryDate: дата окончания срока годности (должна быть в будущем, вводится в формате ММ.ГГГГ, дата должна быть корректной).
* briefInfo: краткая информация о медикаменте (не может быть пустым).
* photo (FK): id фотография медикамента (ссылка на AppPhoto).
* document (FK): id документ медикаментa (ссылка на AppDocument).
* user (FK): id пользователь, которому принадлежит медикамент (ссылка на AppUser).
### BinaryContent
* id (PK): Уникальный идентификатор, генерируется автоматически.
* fileAsArrayOfBytes: Массив байтов. (не может быть пустым).
### AppUser
* id (PK): Уникальный идентификатор, генерируется автоматически.
* telegramUserId: Идентификатор пользователя в Telegram (уникален).
* firstLoginDate: Дата первого входа в приложение (генерируется автоматически). 
* firstName: Имя пользователя (не может быть пустым).
* userName: Имя пользователя в Telegram (уникально).
* email: Электронная почта пользователя (уникальна, должна соответствовать формату электронной почты).
* isActive: Статус активности пользователя (true/false).
* userState: Состояние пользователя.
* medicationInputStep: Текущий шаг ввода медикаментов.
### AppPhoto
* id (PK): Уникальный идентификатор, генерируется автоматически.
* telegramFileId: Идентификатор файла в Telegram (уникален)
* binaryContent_id (FK): Внешний ключ, связывает с таблицей BinaryContent.
* fileSize: Размер файла фотографии (неотрицательно).
### AppDocument
* id (PK): Уникальный идентификатор, генерируется автоматически.
* telegramFileId: Идентификатор файла в Telegram (уникален).
* docName: Наименование документа (не может быть пустым).
* binaryContent_id (FK): Внешний ключ, связывает с таблицей BinaryContent.
* mimeType: Тип содержимого документа.
* fileSize: Размер файла документа (неотрицательно).
## Общие ограничения целостности
* Уникальность идентификаторов в каждой таблице (id).
* Связи между таблицами через внешние ключи.
# Пользовательские роли
### Пользователь
Ответственность: ввод и учет медикаментов, мониторинг сроков годности, корректность вводимой почты.
Количество пользователей: неограниченно.
### Администратор
Ответственность: управление пользователями, доступ к данным.
Количество пользователей: 1.

# UI / API 
Интерфейс пользователя реализуется через Telegram Bot API.
Более подробно с примерами будет описано позже.
# Технологии разработки
## Язык программирования 
Java 11
## СУБД
PostgreSQL
# Тестирование
будет дополнено позже.
