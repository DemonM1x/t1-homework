## Bishop-prototype

- Demo приложение на базе:
``synthetic-human-core-starter``

### Быстрый запуск:

- Сборка gradle:

``gradle clean build``

- Запустите java приложение в IDE.
Докер контейнеры должны запуститься автоматически
с помощью зависимости spring-boot-docker-compose

- Если контейнеры не запустились,
выполните run из файла docker-compose.yml

- Или же выполните
``docker-compose up -d --build``
из docker desktop

### Тест приложения:

Запустим много запросов для проверки
заполнения очереди:

![Postman](bishop-prototype/img/img.png)

Видим, что есть успешные ответы и
ответы с ошибкой 429 - переполнение очереди:

![Postman](bishop-prototype/img/img_1.png)

Рассмотрим тело ответа подробнее: 

Видим подробную информацию в ответе,
сформированную в стартере

![Postman](bishop-prototype/img/img_2.png)

Попробуем отправить дефолтный
запрос с ошибкой в теле запроса -
нам приходит подробный ответ со статусом 400

![Postman](bishop-prototype/img/img_3.png)

Рассмотрим успешное выполнение запроса:

В IDE выводится log сообщения

![Postman](bishop-prototype/img/img_4.png)

![IDE](bishop-prototype/img/img_5.png)

Посмотрим, как сообщение выглядит в kafka.

Воспользуемся приложением kafka-ui:

Сообщение попадает в kafka в удобном
для чтения формате

![Kafka](bishop-prototype/img/img_6.png)

Рассмотрим метрики. Изучить кастомные метрики
можем с помощью grafana, на базе prometheus. 

Можем видеть количество
выполненных тасков андроидом и
количество активных андроидов
![Grafana](bishop-prototype/img/img_7.png)

На данном изображении видем, что у каждого
андроида отдельно отслеживается
число выполненных команд
![Grafana](bishop-prototype/img/img_8.png)
