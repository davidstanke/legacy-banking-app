This is a "legacy" enterprise application, created using Gemini CLI, that conforms to web development standards circa... 2005, maybe? It's intended to be used as an example of how to modernize an application with agentic coding techniques. Start here and then apply a modernization process to get something newer.

# Running the application
* Ensure you have a Java runtime. Anything that supports Java 8 or newer _should_ work.
* run `./mvwn jetty:run`

# TODO
Consider adding some playwright tests that are agnostic to the implementation. These can be run throughout the migration to validate that behavior is unchanged.