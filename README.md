# POC: Java Commands IoC

It demonstrates how to implement a type-safe command handler to interact with devices based on Inversion of Control concept.

The goal is to allow a client to change their device state (turn on, turn off, set log level) via HTTP requests to a REST API informing the command name and its payload serialized in JSON. Regarding the implementation, we want to be able to implement new commands without changing any other command and participant from persistence layer, Web layer or anything else, which means commands are isolated from other commands, perform a single action and to have a unique payload.

We also want the command to be implemented as type-safe and null-safe as possible, which means using Java and Object-Oriented Programming (OOP) concepts (records, classes, interfaces) to represent the command payload instead of using data structures such as List and Map.

The current implementation contains an abstraction to define and implement commands and the tech stack is based on Spring Framework (mostly Context and Web MVC) and SQL database (H2). The application does not contain a Web server, but we have automated tests to every participant and behavior we want to implement.

## Software Design

The design is based on the concept of Inversion of Control (IoC) and is expressed in two main interfaces: `Command` and `CommandPayload`.

`Command` is an interface to represent an action that can be performed. We may have as many implementations as we want as long as they do not depend on any other implementation as can be identified by a name. The current implementation provides two examples: `TurnOn` and `SetLogLevel`.

`CommandPayload` is an empty interface that represents the structure of the data required by a `Command` to be executed. We may have at most two implementations per `Command`. In case a command does not need any data, the `CommandPayload.Empty` record can be used to represent that. The current implementation provides two examples: `SetLogLevel.PayloadInput` and `SetLogLevel.PayloadOutput`.

The interfaces explained above should be used to implement the commands we want to allow the client do execute and validations, but not to represent a concrete command. To represent the command itself we have the `CommandResult` interface which contains attributes such as ID, name, input payload and output payload. The interface is sealed and there are three sequential stages to represent a command: `Processed`, `Persisted` and `Executed`.

The `Processed` result represents a command that were received and contains a valid payload. The `Persisted` result represents a command that was processed and is now persisted on the database. The `Executed` result represents a command that was persisted on the database and is finished.

Commands also have a context, which is also an empty interface named `CommandContext` and it contains any metadata relevant during the command processing stage, such as objects created during the input payload validation or an exception during this process. That allows us to avoid losing information acquired during the input payload validation. We may have at most one implementation per `Command`. Every command having a context means that we should not reuse instances, but create a new one every time we want to interact with devices to avoid leaking the command context.

The REST API implementation is based on three layers: `Repository`, `Service` and `Controller`. The `Repository` is responsible to manage interactions with the SQL database or any other persistence mechanism that implements `CommandRepository` interface. The `Service` is responsible to execute business logic such as command state changed and depends on the `CommandRepository` contract rather than its implementations. The `Controller` is responsible to receive user input and error handling.

The command implementation is provided via `CommandFactory` which is an interface that returns concrete implementations of `Command` interface such as `TurnOn` and `SetLogLevel` by the name. The default implementation is based on Spring Context and the beans must have the prototype scope, so they're recreated every time we want to avoid the context issue mentioned before.

These are the steps to implement new commands:

1. Specify the command name in `CommandName` enum
2. Implement `Command` interface defining the `CommandName` entry created as the command name
3. Create a bean in `CommandsConfiguration` telling Spring Context the command is available
4. Implement `CommandPayload` and `CommandContext` according to the command needs (optional)

No other changes should be required to implement new commands.

## How to run

| Description | Command |
| :--- | :--- |
| Run tests | `./gradlew test` |

