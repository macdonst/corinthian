var corinthian = {
    version: "0.1",
    
    FileDialog: {
        pickFile: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(f) {
                window.resolveLocalFileSystemURI(f, function(fileEntry) {
                    successCallback(fileEntry);                    
                }, fail);
            };
            var fail = typeof errorCallback !== 'function' ? null : function(code) {
                errorCallback(new FileError(code));
            };
            cordova.exec(win, fail, "FileDialog", "pickFile", [options]);
        },    
        pickFolder: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(d) {
                window.resolveLocalFileSystemURI(d, function(dirEntry) {
                    successCallback(dirEntry);                    
                }, fail);
            };
            var fail = typeof errorCallback !== 'function' ? null : function(code) {
                errorCallback(new FileError(code));
            };
            cordova.exec(win, fail, "FileDialog", "pickFolder", [options]);
        }    
    },
    
};
