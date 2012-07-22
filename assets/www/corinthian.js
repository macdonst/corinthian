var corinthian = {
    version: "0.1",
    mediaObjs: {},
    mediaTimers: {},

    FileDialog: {
        pickFile: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(f) {
                window.resolveLocalFileSystemURI(f, function(fileEntry) {
                    successCallback(fileEntry);
                }, fail);
            };
            cordova.exec(win, errorCallback, "FileDialog", "pickFile", [options]);
        },
        pickFolder: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(d) {
                window.resolveLocalFileSystemURI(d, function(dirEntry) {
                    successCallback(dirEntry);
                }, fail);
            };
            cordova.exec(win, errorCallback, "FileDialog", "pickFolder", [options]);
        },
        patch: function() {
            var inputs = document.getElementsByTagName("input");
            for (var i=0; i < inputs.length; i++) {
               if (inputs[i].getAttribute('type') == 'file'){
                   var me = inputs[i];
                   inputs[i].addEventListener("click", function() {
                       corinthian.FileDialog.pickFile(function(fileEntry) {
                           me.value = fileEntry.fullPath;
                       });
                   });
               }
            }
        }
    },
    ContactPicker: {
        choose: function(successCallback, errorCallback) {
            var win = typeof successCallback !== 'function' ? null : function(contact) {
                successCallback(navigator.contacts.create(contact));
            };
            cordova.exec(win, errorCallback, "ContactPicker", "choose", []);
        }
    },
    Video: {
        play: function(url) {
            cordova.exec(null, null, "VideoPlayer", "playVideo", [url]);
        },
        patch: function() {
            var videos = document.getElementsByTagName("video");
            for (var i=0; i < videos.length; i++) {
                var me;
                if (videos[i].src) {
                    me = videos[i].src;
                } else {
                    me = videos[i].firstElementChild.src;
                }
                videos[i].addEventListener("click", function() {
                    corinthian.Video.play(me);
                });
            }
        }
    },
    Audio: {
        patch: function() {
            var audioclips = document.getElementsByTagName("audio");
            for (var i=0; i < audioclips.length; i++) {
                // Create new Media object.
                var audioSrc = audioclips[i].firstElementChild.src;
                if (audioSrc.indexOf("file:///android_asset") == 0) {
                    audioSrc = audioSrc.substring(7);
                }
                corinthian.mediaObjs[audioSrc] = new Media(audioSrc, function() {
                    clearInterval(corinthian.mediaTimers[audioSrc]);
                    document.getElementById('image'+corinthian.mediaObjs[audioSrc].id).src = "images/play.png";
                    document.getElementById('audio_position'+corinthian.mediaObjs[audioSrc].id).innerHTML = "00:00";
                });
                var mediaObj = corinthian.mediaObjs[audioSrc];
                // Create the HTML
                var newAudio = document.createElement('div');
                newAudio.setAttribute("id", "audio"+mediaObj.id);
                var newImg = document.createElement('img');
                newImg.setAttribute('src', 'images/play.png');
                newImg.setAttribute("id", "image"+mediaObj.id);
                newAudio.appendChild(newImg);
                // Set the onclick listener
                newAudio.addEventListener("click", function() {
                    // figure out what image is displayed
                    if (newImg.src.indexOf("images/play.png", newImg.src.length - "images/play.png".length) !== -1) {
                        newImg.src = "images/pause.png";
                        mediaObj.play();
                        // Update media position every second
                        corinthian.mediaTimers[audioSrc] = setInterval(function() {
                            // get media position
                            mediaObj.getCurrentPosition(
                                // success callback
                                function(position) {
                                    var duration = mediaObj.getDuration();
                                    var floor = Math.ceil((position/duration) * 100);
                                    document.getElementById('left'+mediaObj.id).setAttribute('style', "width:"+floor+"%");
                                    document.getElementById('right'+mediaObj.id).setAttribute('style', "width:"+(100-floor)+"%");
                                    if (position > 0) {
                                        var pad = function(t){
                                            if (t < 10) {
                                                return "0" + t;
                                            }
                                            return t;
                                        };
                                        document.getElementById('audio_position'+mediaObj.id).innerHTML = pad(Math.floor(position / 60)) + ":" + pad(Math.floor(position % 60));
                                    }
                                },
                                // error callback
                                function(e) {
                                    console.log("Error getting pos=" + e);
                                }
                            );
                        }, 1000);
                    } else {
                        newImg.src = "images/play.png";
                        mediaObj.pause();
                    }
                });
                var progress = document.createElement('div');
                progress.setAttribute('class', 'dd');
                var left = document.createElement('div');
                left.setAttribute('id', 'left'+mediaObj.id);
                left.setAttribute('class', 'blue');
                left.setAttribute('style', 'width:0%');
                var right = document.createElement('div');
                right.setAttribute('id', 'right'+mediaObj.id);
                right.setAttribute('class', 'red');
                right.setAttribute('style', 'width:100%');
                progress.appendChild(left);
                progress.appendChild(right);
                newAudio.appendChild(progress);
                var duration = document.createElement('span');
                duration.setAttribute('id', 'audio_position'+mediaObj.id);
                duration.innerHTML = "00:00";
                newAudio.appendChild(duration);
                // replace the audio tag with out div
                audioclips[i].parentNode.replaceChild(newAudio, audioclips[i]);
            }
        }
    },
    monkeypunch: function() {
        if (navigator.device.platform == "Android") {
            var scripts = document.getElementsByTagName('script'), len = scripts.length, src, patch = "none";

            while (len--) {
                src = scripts[len].src;
                if (src && src.indexOf("corinthian") > -1) {
                    patch = scripts[len].getAttribute("corinthian-patch");
                    break;
                }
            }

            if (patch === "none") {
                return;
            }
            else if (patch === "all") {
                corinthian.FileDialog.patch();
                corinthian.Video.patch();
                corinthian.Audio.patch();
            }
            else {
                if (patch.indexOf("audio") > -1) {
                    corinthian.Audio.patch();
                }
                if (patch.indexOf("file") > -1) {
                    corinthian.FileDialog.patch();
                }
                if (patch.indexOf("video") > -1) {
                    corinthian.Video.patch();
                }
            }
        }
    }
};

document.addEventListener("deviceready", corinthian.monkeypunch, true);
