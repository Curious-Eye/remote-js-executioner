# Розгляд виконання тестового завдання Pragmasoft для Java розробника. Andrii Lupynos

# Висновки

Вітаю, Андрій

Я переглянув ваше виконання тестового завдання.

В цілому завдання можна вважати виконаним, але є суттєві зауваження, що будуть нижче.

З результату видно, що ви можете аналізувати вимоги, знаєте Spring та Java на достатньому рівні,
можете ефективно розібратись з новим для вас матеріалом та втілити вимоги за адекватний час.
В той же час раджу ретельно проаналізувати та виправити зауваження, щоб підвищити свій рівень як розробника.
Дуже прошу, наступного разу використайте git.

# Вимоги

Write REST API wrapper around the GraalJs javascript interpreter, allowing to:

1. run arbitrary javascript code passed as a request body and return back in response body script's console output and console error (in case of error).
2. review the list of previously executed scripts, their execution statuses (successfully completed, executing, queued, etc..) , other useful context info like scheduled time, execution time, as well as script body and its console output till now. Allow list filtering by status and list ordering by script id, scheduled time
3. forcibly stop any script to prevent hanging scripts from consuming system resources.
4. remove inactive (stopped, completed, failed) scripts from the list by their id.

Keep in mind that the REST api design should allow concurrent requests. Also we cannot control the script's content - it can be broken, malicious, malformed, or contain infinite loops. We should provide to the script authors sufficiently detailed diagnostic information about script errors, if any.

Authentication is not required - API should be public for simplicity. It's not required to write a client for this API.You can use postman and/or curl for testing. Unit tests are also welcome. Use maven or gradle for build.

Provide adequate code documentation and build / run instructions.

# Зауваження

## Виконання вимог

Наступні вимоги **не** були виконані :

> run arbitrary javascript code passed as a request body

Дизайн вашого API вимагає передати скрипт на виконання у вигляді JSON а не text/plain. Це ускладнює передачу та перегляд більш-менш нетривіальних скриптів з більш ніж одного рядка та маючих вирівнювання.

> execution time

Замість цього доводиться вираховувати його з begin та end date, що досить незручно

> remove inactive (stopped, completed, failed) scripts from the list by their id.

цього немає

> We should provide to the script authors sufficiently detailed diagnostic information about script errors, if any.

це не зроблено належним чином.

Наприклад інформація про синтаксичні помилки зовсім відсутня:

```
// unbalanced parentheses
{ "code": "\nlet i = 0;\nwhile (i<10000 {\nconsole.log(i++);\n};"}

{
  "path": "/api/tasks",
  "status": 400,
  "error": "Bad Request",
  "requestId": "9ceff201-7"
}
```

Також runtime errors мають втрачені stack traces, causes, etc.

Взагалі stderr не збирається належним чином, тож якщо скрипт сам обробляє свої помилки через вивід діагностики на stderr ця інформація буде втрачена:

```
{
  ...
  "code": "console.error('My error')",
  "output": "",
  "error": null,
  "status": "COMPLETED",
  ...
}
```

> Provide adequate code documentation and build / run instructions.

Код документований, але API документація відсутня. Було б непогано або зробити документацію через Swagger у OpenAPI форматі, або
хоча б перелічити у HELP.md ендпойнти та приклади застосування з допомогою curl, щоб не доводилось це вишукувати по коду. Навіть наявність HATEOAS лінків значно покращила б юзабіліті API.

## Дизайн API

1. Відсутність HATEOAS

2. JSON не єдиний у світі формат, я б сміливіше використовував plaintext subresources, like `/api/tasks/{id}/code`, тут це доречно.

3. Дивитись у списку `/api/tasks` повний output та code досить незручно. Для елементів списку потрібна скорочена модель

```
 {
    "id": "ea3eeb0c-95f5-4342-94e1-244a7bff77f0",
    "name": "1d44144c-4ff9-4312-8fe9-5fc8ae3a265c",
    "code": "\nlet i = 0;\nwhile (i<10000) {\nconsole.log(i++);\n};",
    "output": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n30\n31\n32\n33\n34\n35\n36\n37\n38\n39\n40\n41\n42\n43\n44\n45\n46\n47\n48\n49\n50\n51\n52\n53\n54\n55\n56\n57\n58\n59\n60\n61\n62\n63\n64\n65\n66\n67\n68\n69\n70\n71\n72\n73\n74\n75\n76\n77\n78\n79\n80\n81\n82\n83\n84\n85\n86\n87\n88\n89\n90\n91\n92\n93\n94\n95\n96\n97\n98\n99\n100\n101\n102\n103\n104\n105\n106\n107\n108\
```

4. Ендпойнти `/tasks/actions/find-by-name` або `/tasks/{id}/stop-execution` не відповідають REST принципам. Натомість це RPC стиль. Його головний недолік - нестабільність інтерфейсу між клієнтом та сервером, бо кожний новий action вимагає нового ендпойнту та моделі dto. Якщо буде цікаво, можемо потім обсудити детальніше.

5. Не використовуються заголовки кешування, умовного завантаження, компресії, безпеки та ін.

6. `task.name` дублює `task.id`, я б залишив тільки щось одне.

## Дизайн архітектури

1. Невиправдана переускладненість реалізації. Найбільш швидкий код - це код, який взагалі не написаний. Використання reactor для цього проекту не дало суттєвих переваг, але ускладнило читання коду та призвело до додаткових помилок. Я розумію, що ви мабуть нещодавно його вивчили, а у такій ситуації коли в тебе є молоток усе здається цвяхом, але тоді треба було більше використовувати хоч якісь переваги, наприклад hot publisher замість polling, event driven architecture, CQRS.

Окрім цього, ознаки цієї проблеми є також у використанні двох коллекцій для зберігання тасків, що викликає необхідність їх координації, на мій погляд зайвої. Занадто багато дублюючих representatons of Task concept потребують зайвого коду по конверсії між ними, до того ж ручної (чули про MapStruct ?)

Використання lombok також має свої недоліки https://reflectoring.io/when-to-use-lombok/#use-lombok-with-caution

2. Я оцінив, що ви цікавитесь архутектурою та новими фреймворками, намагались зробити DDD, але з DDD на мій погляд не впорались. Головна ознака цього - анемічна доменна модель (антипаттерн https://thedomaindrivendesign.io/anemic-model/) але є й інші проблеми. Також можу більш детально розповісти голосом якщо цікаво.

3. Скріпт не ставиться в чергу одразу, використовується двохсекундний поллінг. Це погіршує продуктивність, ускладнює та уповільнює тести.

4. Неузгоджений стан скрипта у деяких випадках. Наприклад `/api/tasks` не виводить актуальний output на відміну від `/api/tasks/{id}`. Також дивіться наступну проблему з timeout

5. 30 seconds timeout взагалі дуже спірне рішення. Для деяких скриптів це дуже багато, а для деяких навпаки замало - деякі скрипти можуть годинами виконуватись. Але у вашому випадку це ще й не працює. Спробуйте запустити десь 20 "безкінечних" скриптів
   `while (true) {}` та подивитись, що робиться, та зрозуміти чому.

6. Використання GraalJSScriptEngine має одразу декілька проблем.

6.1. Він є AutoCloseable але ніде не закривається як потрібно (try with resources)

6.2. Він використовується як конкретний клас, а не через його інтерфейс, що порушує DI і також ініціалізується з порушенням DI.
До речі, сервіси також використовуються не через інтерфейси.

6.3. У TaskValidateService він використовується потоконебезпечно (not thread safely).

6.4. Він є досить "важким", щоб створювати та знищувати його при кожному запросі. У таких випадках зазвичай використовують пул, на кшталт пулу тредів. Thread local or thread scoped may also work if used with thread pool.

7. Обробка помилок. Багато де зроблена помилково (втрачається важлива інформація) або не зроблена зовсім (зупинення неіснуючого
   скрипта повинно повертати 404). Я б рекомендував активніше використовувати Domain Exceptions на більш нижчих рівнях (Repository, Service) та обробляти їх у `@ControllerAdvice` на найвищому рівні, щоб запобігти дублюванню.

8. `CompiledScript` який створюється при валідації можна використовувати для виконання, це буде швидше, ніж новий eval скрипта як строки.

9. Багато модифікацій виконуються некоректно.
   Багато такого коду, як наведений нижче (перевірка та наступна модифікація) повинні робитись у атомарному блоці (synchronized або exclusive Lock acquired).

```
      if (task.getStatus() == TaskStatus.EXECUTING)
          task.setOutput(taskExecuteService.getCurrentExecutionOutput(task.getId()));
```

Невиконання цього правила може привести до неузгодженого стану або дедлоків.

10. Повертати мутабельний об'єкт на який також зсилається репозіторій - це дуже небезпечна практика особливо у конкурентному середовищі.

```
public Mono<TaskEntity> findById(String id) {
        return Mono.fromCallable(() -> taskStore.get(id));
    }
```
