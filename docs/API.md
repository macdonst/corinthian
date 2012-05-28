API Documentation
=================

* ContactPicker
* FileDialog
* Video
* Audio

ContactPicker
=============

The contact picker allows the user to select a single contact by using the native Android UI to display the list of contacts. Once the users clicks on a contact they success callback will be executed with a fully populated Contact object.

methods
-------

    corinthian.ContactPicker.choose(successCallbac, failureCallback)

Quick Example
-------------
    corinthian.ContactPicker.choose(function(contact) {
        alert("contact is " + contact.name.formatted);
    }, function() {
        console.log("Unable to select contact");
    });

Full Example
------------

FileDialog
==========

The FileDialog relies on the OI File Manager to provide a graphical user interface for selecting a file or folder. Once the user selects a file or folder the success callback will be executed with a FileEntry in the case of pickFile or DirectoryEntry in the case of pickFolder.

As an added bonus Corinthian scans your HTML document and monkey patches any instances of &lt;input type="file"/&gt;. When the user clicks on the input tag it will launch the FileDialog.

methods
-------

    corinthian.FileDialog.pickFile(successCallback, failureCallback)
    corinthian.FileDialog.pickFolder(successCallback, failureCallback)

Quick Example
-------------

    corinthian.FileDialog.pickFile(function(fileEntry) {
        console.log("path is " + fileEntry.fullPath);
    }, function(error) {
        console.log("Unable to select file);
    }, options);


Full Example
------------

VideoPlayer
===========

In the Android WebView the &lt;video/&gt; tag is very broken. In order to work around these issues I created the VideoPlayer plugin. The play method invokes the Android video player in landscape mode. It can play videos from the SD card, assets folder, a HTTP address or a YouTube video.

As an added bonus Corinthian scans your HTML document and monkey patches any instances of &lt;video/&gt;. When the user clicks on the video tag it will launch the video player. My recommendation is for the user to provide a poster for the web view to show before the video is played as it makes the UI look better.

methods
-------

    corinthian.Video.play(url)

Quick Example
-------------

    corinthian.Video.play("file:///sdcard/MyMovie.mp4")
    corinthian.Video.play("file:///android_asset/www/file.mp4")
    corinthian.Video.play("http://path.to.my/file.mp4")
    corinthian.Video.play("http://www.youtube.com/watch?v=E0UV5i5jY50")

Full Example
------------

Audio
=====

methods
-------

Quick Example
-------------

Full Example
------------
