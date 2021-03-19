# Display JavaHelp from Apache NetBeans in your local Browser

JavaHelp is an abandoned framework to display an "Online Help" in a
Java SE Swing application. This system was used in NetBeans IDE and RCPs before the
donation of the project to the Apache Foundation. Due to license issues this system
had to be dismissed an is not replaced since.

This project is an attempt to replace the default JavaHelp system in Apache NetBeans
with a local webserver and tries to reuse as much from the old JavaHelp pages as
possible. The default implementation in this stand-alone module just sends the
existing HTML file, stored in a `nbdocs:` location to the browser.

:warning: The project is currently in an early alpha state. This means there is no "release"
yet, and the API may change in any direction.


## Implement an own Renderer

To replace the default "Renderer" you have to implement the inface
`de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpTemplate` on your own class and register that
implementation via `@ServiceProvider` as mentioned in `HelpTemplate`. Currently the
method `renderHelpPage` will take a `HelpRenderContext`, but this will change in one
of the next iterations as the "Context" only consists of the `URL` of the help resource
and an `OuputStream` to write to.

The Renderer is in charge for the full HTML output to the browser. All other resources
like images, scripts or style sheets are send to the browser directly.


## Other static resources

You can provide other resources like HTML pages or images that are not part of the
`nbdocs:` universe. These are all resources in the "root" of the local webserver and
everything inside the path `/nb-help/`. The latter path can be branded in an
RCP application (see `de.rwthaachen.wzl.gt.nbm.nbhelp.HelpProxy`).

The interface `de.rwthaachen.wzl.gt.nbm.nbhelp.api.HelpContentHandler` is used to
register implementations that can "register" itself as "path handlers". They must
create an `URLConnection`, but there is a utility class `SimpleTextGenerator` in the API
that can be used to wrap any text content.

If you plan to provide your own `/index.html` you should register your implementation
below `position=100` as a default page is registered at that position.


## Accessing the registered Helpsets

The default implementations of the JavaHelp classes are not used directly by this module.
Only the `HelpDisplayer` class has to deal with some of the classes to handle the
help requests from the user. For all implementations using this project the `data` package
contains "all" nessesary data. The main accessor class is `HelpsetManager` if you
want to find other help IDs or the registered `HelpSet`s.

The module itself does only provide a default `/index.html` which displays the entry
points of all registered `HelpSet`s. No navigation or usage of the `View`s are implemented.
