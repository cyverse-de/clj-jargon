# clj-jargon

## Testing from the REPL

This library is configured to add the path `./repl` to the source file search path in REPL mode, so that developers can
easily test using the Clojure REPL. To use this feature, first create two files: `$HOME/.irods/.qa-jargon.json` for QA,
and `$HOME/.irods/.prod-jargon.json` for production. Note that these files will contain sensitive connection information
for iRODS. Be sure to set the file permissions accordingly. Each file should look like this:

``` json
{
    "host": "somehost.example.org",
    "zone": "example",
    "port": "1247",
    "user": "someuser",
    "password": "S0m3-P@$$w0rd",
    "home": "/example/home",
    "resource": "",
    "max-retries": 10,
    "retry-sleep": 1000,
    "use-trash": true
}
```

Once you have the files in place, you can easily create a Jargon configuration by calling either `init-prod` or
`init-qa` from `clj-jargon.repl-utils`:

```
user=> (require '[clj-jargon.repl-utils :as ru])
nil

user=> (def cfg (ru/init-prod))
#'user/cfg
```

Once you have the configuration map, you can use it to obtain information about items in the data store:

```
user=> (require '[clj-jargon.init :refer [with-jargon]])
nil

user=> (require '[clj-jargon.item-info :refer [stat]])
nil

user=> (with-jargon cfg [cm] (stat cm "/example/home/a"))
{:id "/example/home/a", :path "/example/home/a", :type :dir, :date-created 1398189130000, :date-modified 1610580321000}
```

The `repl` directory is also included in `.gitignore` so that you can add additional namespaces to it without having to
worry very much about accidentally including them in a commit. This can be very helpful if you have an editor that can
interactively run Clojure code.
