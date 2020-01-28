LOGSCAPE_URL = 'http://0.0.0.0:8080'
DEFAULT_TENANT='logscape-test-storage'
//LOGSCAPE_URL = 'https://5er31crao2.execute-api.eu-west-2.amazonaws.com/Prod'
KEY = '5b578yg9yvi8sogirbvegoiufg9v9g579gviuiub8' // not real

$(document).ready(function () {
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
//            .error(function (xhr, ajaxOptions, thrownError) {
//                        alert(xhr.status);
//                        alert(thrownError);
//              })

    }
      removeImportFromStorage(storageId, tags, includeFileMask) {
                $.get(LOGSCAPE_URL + '/storage/removeImported', {tenant:DEFAULT_TENANT, storageId: storageId, includeFileMask: includeFileMask, tags: tags},
                    function(response) {
                        $.Topic(Logscape.Explorer.Topics.removedImportFromStorage).publish(response);
                    })
    //            .error(function (xhr, ajaxOptions, thrownError) {
    //                        alert(xhr.status);
    //                        alert(thrownError);
    //              })

        }

//let pakoGzCompressor = window.pako;
    fileContents(filename) {
        // jquery ajax binary support is missing - use standard JS
        if (filename.endsWith(".gz")) {
            let url = LOGSCAPE_URL + '/query/get/' +  encodeURIComponent(DEFAULT_TENANT) + "/" + encodeURIComponent(filename)
            var oReq = new XMLHttpRequest();
            oReq.open("GET", url, true);
            oReq.responseType = "blob";

            oReq.onload = function(oEvent) {
              let blob = oReq.response;
//              let arrayBuffer = await new Response(blob).arrayBuffer()
              var reader = new FileReader();
              reader.readAsArrayBuffer(blob);
              reader.onloadend = (event) => {
                  // The contents of the BLOB are in reader.result:
                  var byteArrayStuff = reader.result;
                  let textyBytes = pako.inflate(byteArrayStuff);
                  var explodedString = new TextDecoder("utf-8").decode(textyBytes);
                  $.Topic(Logscape.Explorer.Topics.setFileContent).publish(explodedString);
                }
              }
              // ...
            oReq.send();
        } else {
            $.get(LOGSCAPE_URL + '/query/get/' +  encodeURIComponent(DEFAULT_TENANT) + "/" + encodeURIComponent(filename),{},
                function(response) {
                    $.Topic(Logscape.Explorer.Topics.setFileContent).publish(response);
                })
//            .error(function (xhr, ajaxOptions, thrownError) {
//                        alert(xhr.status);
//                        alert(thrownError);
//              })
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
        backend.fileContents(event);
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

