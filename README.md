# Keycloak (Quarkus 26.2.3) Phone Provider
+ Регистрация и логин по номеру телефона любого оператора
+ Отправка OTP по СМС через http endpoint
+ Подтверждение OTP через http endpoint

## Запуск локально
1) Добавить однострочный файл .env по пути: keycloak-phone-provider/docker/.env с содержимым GRAVITEE_API_KEY=...
2) Поправить путь к volumes в docker-compose.yml
3) Запустить скрипт start-dev.sh

## Запуск на проде
1) Задать GRAVITEE_API_KEY=... в environment переменных сервера на котором будет запущен контейнер или задать напрямую в docker-compose.yml

### Список возможных команд запуска и их описание:
```bash
Общие:
--spi-phone-default-service=[dummy|mrc|...]  # Выбор провайдера SMS: dummy (заглушка), mrc (реальный сервис) или другой кастомный  
--spi-phone-default-token-expires-in=60  # Время жизни OTP-кода в секундах. По умолчанию: 60 секунд  
--spi-phone-default-source-hour-maximum=10  # Максимальное количество SMS с одного IP-адреса в час. 0 — без ограничений. По умолчанию: 10  
--spi-phone-default-target-hour-maximum=3  # Максимальное количество SMS на один номер телефона в час. 0 — без ограничений. По умолчанию: 3  
--spi-phone-default-[$realm-]duplicate-phone=false  # Разрешить привязку одного номера телефона к нескольким пользователям. По умолчанию: false (запрещено)  
--spi-phone-default-[$realm-]default-number-regex=^\+?\d+$  # Регулярное выражение для проверки формата номера после приведения к стандарту. Примеры форматов: INTERNATIONAL (+41 44 668 18 00), NATIONAL (044 668 18 00), E164 (+41446681800)  
--spi-phone-default-[$realm-]valid-phone=true  # Проверять валидность номера телефона через библиотеку libphonenumber. По умолчанию: true. Если включено — номера будут парситься и приводиться к каноническому формату (E164 и др.). Необходимо для корректного обнаружения дубликатов
--spi-phone-default-[$realm-]canonicalize-phone-numbers=E164  # Формат нормализации телефонных номеров: E164, INTERNATIONAL, NATIONAL, RFC3966. По умолчанию: пусто — не нормализуется. 
# E164: +79991234567    
# INTERNATIONAL: +7 999 123-45-67    
# NATIONAL: 8 (999) 123-45-67    
# RFC3966: tel:+7-999-123-45-67
--spi-phone-default-[$realm-]phone-default-region=US  # Регион по умолчанию при парсинге номера (если не указан код страны). По умолчанию: использует локаль реалма. Коды регионов: https://www.unicode.org/cldr/cldr-aux/charts/30/supplemental/territory_information.html
--spi-phone-default-[$realm-]compatible=false  # Совместимый режим: при поиске пользователя учитывать все форматы сохранённого номера. По умолчанию: false. Если true — поиск будет находить пользователя по любому формату номера (E164, NATIONAL и т.п.)
--spi-phone-default-[$realm-]otp-expires=3600  # Время, в течение которого 2FA считается подтверждённым (в секундах). По умолчанию: 3600 (1 час). Позволяет избежать повторного ввода OTP при каждом входе в течение указанного времени

Отправка СМС:
	Урлы
--spi-message-sender-service-mrc-sms-send-url=https://host.docker.internal:8983/sms  # URL внешнего сервиса для отправки SMS через MRC (Gravitee или другой шлюз)
--spi-phone-verification-code-default-otp-verify-url=https://host.docker.internal:8983/otp  # URL для проверки (валидации) OTP-кода на внешнем сервисе

Http клиент:
	Таймауты
--spi-connections-http-client-default-connect-timeout-millis=  # Время ожидания установки TCP-соединения (в миллисекундах)
--spi-connections-http-client-default-socket-timeout-millis=  # Время ожидания ответа от сервера после установки соединения (в миллисекундах)
--spi-connections-http-client-default-connection-request-timeout-millis=  # Время ожидания получения соединения из пула (в миллисекундах)

	Пул
--spi-connections-http-client-default-max-connections=200  # Общее максимальное количество HTTP-соединений в пуле. По умолчанию: 200
--spi-connections-http-client-default-max-connections-per-route=50  # Максимальное количество соединений на один маршрут (host:port). По умолчанию: 50

	Прокси
--spi-connections-http-client-default-proxy-host=proxy.server  # Адрес HTTP/SOCKS-прокси для исходящих запросов
--spi-connections-http-client-default-proxy-port=8080  # Порт прокси-сервера
--spi-connections-http-client-default-proxy-username=user  # Имя пользователя для аутентификации в прокси (если требуется)
--spi-connections-http-client-default-proxy-password=pass  # Пароль для аутентификации в прокси

	SSL / HTTPS
--spi-connections-http-client-default-disable-trust-manager=true  # Отключить проверку SSL-сертификатов (небезопасно!). Используется для тестирования с самоподписанными сертификатами
# ВНИМАНИЕ: Никогда не включать в продакшене!
```

### Пример команды запуска (нужные значения подставить):
```bash
docker run --name keycloak -p ...:... -e KEYCLOAK_ADMIN=... -e KEYCLOAK_ADMIN_PASSWORD=... galeofan/keycloak:26.2.3_phone-2.5.0 start
--spi-phone-default-service=mrc
--hostname=...
# Если нужно http
--http-port=...
--http-enabled=true
###################
--https-port=...
--spi-phone-default-source-hour-maximum=0
--spi-phone-default-target-hour-maximum=0
--spi-message-sender-service-mrc-sms-send-url=...
--spi-phone-verification-code-default-otp-verify-url=...
```
## Настройки UI
1) Создать новый Realm
2) В Realm settings -> Themes -> Login Theme выбрать тему phone
   ![Форма входа с темой phone](examples/phone-login.png)
3) Обязательно отключить проверку обязательности полей email, firstName, lastName в Realm settings -> User Profile -> Edit -> Required field
   ![Отключение обязательных полей](examples/off-required1.png)
   ![Отключение обязательных полей2](examples/off-required2.png)
4) В Authentication создать новый flow
   Обязательно!!! назвать его Browser with phone
   Назначить его как browser flow
   ![Настройка browser flow1](examples/browser1.png)
   ![Настройка browser flow2](examples/browser2.png)
   ![Настройка browser flow3](examples/browser3.png)
5) В Authentication создать новый flow
   Обязательно!!! назвать его Registration with phone
   Назначить его как registration flow
   ![Настройка registration flow1](examples/register1.png)
   ![Настройка registration flow2](examples/register2.png)
   ![Настройка registration flow3](examples/register3.png)
   ![Настройка registration flow4](examples/register4.png)
6) В Clients->[$client-]-> client_scopes задать phone scope by default чтобы в токене возвращалось:
```
  "scope": "profile phone",
  "phone_number_verified": true,
  "phone_number": "+79606022956",
```
![Возврат в токене](examples/phone-return.png)
7) Задать Authentication -> Required Actions -> Update profile
![Настройка browser flow3](examples/update-profile.png)
