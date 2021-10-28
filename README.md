# CITelegramBot

This is an example application of Telegram bot that can notify users about CI build results and is written in Scala 3.

To run this appliction please setup next environment variables first:
* `PORT`                      - port on which your application runs.
* `JDBC_DATABASE_URL`         - full jdbc format url of your database, e.g.
* `jdbc:postgresql://$host:$port/$dbName?password=qwerty&user=1234`
* `WEBHOOK_HOST_BASE_URL`     - base host url for webhooks, it will be used to supply bot user with a full webhook url.
* `WEBHOOK_REQUEST_URL_SCHEME` - sheme to be used in a webhook requests, it will be used to supply bot user with a full webhook url.

When all env vars setup properly you could run your application using `sbt run`.

## Deployment
This application is suited to be run in different environments, it uses `sbt-native-packager` so you could package your application using `sbt stage` command. It is also suitable to be run on `heroku`, see more info on [heroku documentation](https://devcenter.heroku.com/articles/getting-started-with-scala).

## CI systems support
Currently this project supports only TravisCI, but it is written in the way that allows you to add new integrations in a fairly simple way.

### Security
Be aware that there are no signature checks on webhook requests yet.
