corinthian
==========

Provide some common UI elements to Apache Cordova Android applications.

Welcome
=======

Not unlike rich Corinthian Leather the Corinthian project aims to provide more advanced UI elements for applications built with Apache Cordova. Initially Cordova will target the Android platform but as time goes own we'll add support for other platforms.

Philosophy
==========

We think Apache Cordova is great but there are some things that it doesn't address as they are outside of it's bailiwick. One of those items is a rich UI. Sometimes building Apache Cordova applications is more difficult than it could be as it doesn't (nor shouldn't) provide UI components. The philosophy of the Corinthian project is to provide some of these UI components like contact and file pickers as well as monkey patching the &lt;audio/&gt; and &lt;video/&gt; tags so you won't have to change your HTML code when porting your application to Android.

Future Plans
============

Right now the target is Android but over time we'll migrate these native Android UI components into JavaScript so that it will be compatible with other platforms.

Installation
============

Things will get easier soon but for now:

* Run **ant jar**
* Copy the corinthian-0.1.0.jar file to your Android project's lib directory
* Copy the assets/www/corinthian.js to your Android projects assets/www directory
* Add the following lines to your res/xml/plugins.xml file:
> &lt;plugin name="FileDialog" value="com.simonmacdonald.corinthian.FileDialog"/&gt;
&lt;plugin name="ContactPicker" value="com.simonmacdonald.corinthian.ContactPicker"/&gt;
&lt;plugin name="VideoPlayer" value="com.simonmacdonald.corinthian.VideoPlayer"/&gt;
* Finally link the corinthian.js file in your HTML file using the script tag.
>&lt;script type="text/javascript" charset="utf-8" src="corinthian.js"/&gt;

Issues
======

If you would like to make a feature request of if you've found a bug please open it on our issues page.

Contributing
============

Clone the git repository for the project.
Send a pull request on GitHub with your changes.

Authors and Contributors
========================

Simon MacDonald (@macdonst) - Project creator