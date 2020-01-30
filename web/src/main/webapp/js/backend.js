
$(document).ready(function () {
$.ajaxSetup({
//    crossDomain: true
//    ,
//    xhrFields: {
//        withCredentials: true
//    }//,
//    username: 'test',
//    password: 'test'
});

    binding()

});

class FilesInterface {

    upload(file) {
        throw new err ("not implemented")
    }

    listFiles() {
        throw new err ("not implemented")
    }

    fileContents(filename) {
        throw new err ("not implemented")
    }
    importFromStorage(storageId, includeFileMask, tags) {
        throw new err ("not implemented")
    }
}

var testFiles = [
    { filename:'/this/is/a/file.txt', tags:'prod, uk', resource:'someHost', sizeBytes:100, from:'10:00.21', to:'14:02.34'},
    { filename:'/this/is/a/fileB.txt', tags:'prod, uk', resource:'someHost', sizeBytes:100, from:'10:13.33', to:'15:55.34'}
]

var fixturedFiles = new Map([
        [testFiles[0].name, testFiles[0]],
        [testFiles[1].name, testFiles[1]],
    ]
);

 function downloadBinaryDataFromURL(url, filename){
        var oReq = new XMLHttpRequest();
                oReq.open("GET", url, true);
                oReq.responseType = "blob";

                oReq.onload = function(oEvent) {

                    $.Topic(Logscape.Explorer.Topics.setFileContent).publish("expanding gz...");
                  let blob = oReq.response;
                  var reader = new FileReader();
                  reader.readAsArrayBuffer(blob);
                  reader.onloadend = (event) => {
                      var byteArrayStuff = reader.result;
                      let textyBytes = pako.inflate(byteArrayStuff);
                      var explodedString = new TextDecoder("utf-8").decode(textyBytes);
                      $.Topic(Logscape.Explorer.Topics.setFileContent).publish(explodedString);
                    }
                  }
                oReq.send();
    }

class FilesFixture extends FilesInterface {

    listFiles() {
        let results = new Array();
        fixturedFiles.forEach((value, key, map) => {
            results.push(value)
        });
        $.Topic(Logscape.Explorer.Topics.setListFiles).publish(results);
    }

    fileContents(filename) {
        $.Topic(Logscape.Explorer.Topics.setFileContent).publish(
            fixturedFiles.get(filename).name + " made up file contents from the test fixture"
        );
    }
    importFromStorage(storageId, includeFileMask, tags) {
        throw new err ("not implemented")
    }
}


class RestVersion extends FilesInterface {

    listFiles() {
        // return files
        $.get(LOGSCAPE_URL + '/query/list', {},
            function(response) {
                $.Topic(Logscape.Explorer.Topics.setListFiles).publish(response);
            }
        )
    }
    importFromStorage(storageId, tags, includeFileMask) {
            $.get(LOGSCAPE_URL + '/storage/import', {tenant:DEFAULT_TENANT, storageId: storageId, includeFileMask: includeFileMask, tags: tags},
                function(response) {
                    $.Topic(Logscape.Explorer.Topics.importedFromStorage).publish(response);
                })
            .fail(function (xhr, ajaxOptions, thrownError) {
                        alert(xhr.status);
                        alert(thrownError);
              })

    }
      removeImportFromStorage(storageId, tags, includeFileMask) {
                $.get(LOGSCAPE_URL + '/storage/removeImported', {tenant:DEFAULT_TENANT, storageId: storageId, includeFileMask: includeFileMask, tags: tags},
                    function(response) {
                        $.Topic(Logscape.Explorer.Topics.removedImportFromStorage).publish(response);
                    })
                .fail(function (xhr, ajaxOptions, thrownError) {
                            alert(xhr.status);
                            alert(thrownError);
                  })

        }




/**
* The S3 bucket will needs CORS enabled for direct downloads to work. If it fails it retried by using a lambda request
**/
    fileContentsByURL(filename) {
        $.Topic(Logscape.Explorer.Topics.setFileContent).publish("loading...");
        $.get(LOGSCAPE_URL + '/query/getDownloadUrl/' +  encodeURIComponent(DEFAULT_TENANT) + "/" + encodeURIComponent(filename),{},
            function(urlLocation) {
                try {
                    if (filename.endsWith(".gz")) {
                        downloadBinaryDataFromURL(urlLocation, filename);
                    } else {
                     $.get(urlLocation,{},
                        function(responseContent) {
                            $.Topic(Logscape.Explorer.Topics.setFileContent).publish(responseContent);
                        })
//                        .fail(function(error, anotherone, more) {
//                            console.log("Failed to load URL: " + urlLocation + " error:" + error)
//                            console.log(error)
//                            console.log("Going to load via Lambda function")
//                            fileContents(filename)
//                        })
                    }
                } catch (err) {
                    console.log("Failed to load by signed URL - reverting to Lambda")
                    $.Topic(Logscape.Explorer.Topics.setFileContent).publish("load by URL failed - falling back ... still loading...");

                    fileContents(filename)
                }
            })
    }
    fileContents(filename) {
        // jquery ajax binary support is missing - use standard JS
        $.Topic(Logscape.Explorer.Topics.setFileContent).publish("loading...");
        if (filename.endsWith(".gz")) {
            let url = LOGSCAPE_URL + '/query/get/' +  encodeURIComponent(DEFAULT_TENANT) + "/" + encodeURIComponent(filename)
            downloadBinaryDataFromURL(url, filename);
        } else {
            $.get(LOGSCAPE_URL + '/query/get/' +  encodeURIComponent(DEFAULT_TENANT) + "/" + encodeURIComponent(filename),{},
                function(response) {
                    $.Topic(Logscape.Explorer.Topics.setFileContent).publish(response);
                })
            .fail(function (xhr, ajaxOptions, thrownError) {
                        alert(xhr.status);
                        alert(thrownError);
              })
        }

    }

    downloadFileContent(filename) {
        console.log("Downloading:" + LOGSCAPE_URL + '/query/download/' + DEFAULT_TENANT + '/'  + filename)
            window.open(
              LOGSCAPE_URL + '/query/download/' + encodeURIComponent(DEFAULT_TENANT) + '/'  + encodeURIComponent(filename)
            )
    }
}

function binding () {
    // let backend = new FilesFixture();
    let backend = new RestVersion();

    console.log("Backend is using:" + backend.constructor.name)

    $.Topic(Logscape.Explorer.Topics.getListFiles).subscribe(function(event) {
        backend.listFiles();
    })
    $.Topic(Logscape.Explorer.Topics.getFileContent).subscribe(function(event) {
//        backend.fileContents(event);
        backend.fileContentsByURL(event);
    })

    $.Topic(Logscape.Explorer.Topics.downloadFileContent).subscribe(function(event) {
        backend.downloadFileContent(event);
    })

    $.Topic(Logscape.Explorer.Topics.importFromStorage).subscribe(function(storageId, includeFileMask, tags) {
        backend.importFromStorage(storageId, includeFileMask, tags);
    })
    $.Topic(Logscape.Explorer.Topics.removeImportFromStorage).subscribe(function(storageId, includeFileMask, tags) {
        backend.removeImportFromStorage(storageId, includeFileMask, tags);
    })

}

