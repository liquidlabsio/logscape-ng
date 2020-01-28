// define pub-sub topics
Logscape.Explorer.Topics = {
    uploadFile: 'explorerUploadFile',
    getListFiles: 'explorerGetListFiles',
    setListFiles: 'explorerSetListFiles',
    getFileContent: 'explorerGetFileContent',
    setFileContent: 'explorerSetFileContent',
    importFromStorage: 'importFromStorage',
    importedFromStorage: 'importedFromStorage',
    removeImportFromStorage: 'removeImportFromStorage',
    removedImportFromStorage: 'removedImportFromStorage',

    downloadFileContent: 'explorerDownloadFileContent'
}

$(document).ready(function () {

    let fileList = new Logscape.Explorer.FileList($('#explorerFileListTable'));

    let editor = ace.edit("explorerEditor");
    editor.setTheme("ace/theme/monokai");
    editor.session.setMode("ace/mode/javascript");
    editor.session.setUseWrapMode(true);


    $.Topic(Logscape.Explorer.Topics.setFileContent).subscribe(function(content) {
        $("#explorerOpenFileName").get(0).scrollIntoView();
        editor.setValue(content)
    })


    $('#refreshFiles').click(function(){
        $.Topic(Logscape.Explorer.Topics.getListFiles).publish("doit")
    });
    $.Topic(Logscape.Explorer.Topics.getListFiles).publish("get file list on page load")

    $('.zoomExplorer').click(function(event){
            let zoomDirection = $(event.currentTarget).data().zoom;

//            console.log(dd)
            let normalClass = "normalSizeEditor";
            let mediumClass = "mediumSizeEditor";
            let largeClass = "largeSizeEditor";
            let editor = $('#explorerEditor')
            if (editor.hasClass(normalClass)) {
                if (zoomDirection == "in") {
                    editor.removeClass(normalClass)
                    editor.addClass(mediumClass)
                } else {
                // already at normal size
                }
            } else if (editor.hasClass(mediumClass)) {
                editor.removeClass(mediumClass)
                if (zoomDirection == "in") {
                    editor.addClass(largeClass)
                } else {
                    editor.addClass(normalClass)
                }
            } else if (editor.hasClass(largeClass)) {
              if (zoomDirection == "in") {
              } else {
                 editor.removeClass(largeClass)
                  editor.addClass(mediumClass)
              }
            }
            $("#explorerOpenFileName").get(0).scrollIntoView();
            return false;
    })

});

Logscape.Explorer.FileList = function (table) {
    console.log("Logscape.Explorer.FileList created")

    let dataTable
    let sources

    bindIdsToTable()

    function bindIdsToTable() {

        dataTable = table.dataTable(
            {
                bLengthChange: false,
                sPaginationType: "full_numbers",
                iDisplayLength: 20,
                aoColumns: [
                    { mData: "filename", bVisible: true },
                    { mData: "tags" },
                    { mData: "resource" },
                    { mData: "size" },
                    { mData: "from" },
                    { mData: "to" },
                    { mData: "actions" }
                ]
            })
    }

    $('#explorerFileListTable').on('click','td', function (event) {
        try {
            let filename = dataTable.api().row( this ).data().filename;
            let cell = dataTable.api().cell( this )
            let action = $(event.target).data().action

            if (action == "view") {
                $("#explorerOpenFileName").text("Filename: " + filename)
                $.Topic(Logscape.Explorer.Topics.getFileContent).publish(filename)
            } else if (action == "download"){
                $.Topic(Logscape.Explorer.Topics.downloadFileContent).publish(filename)
            } else {
                $("#explorerOpenFileName").text("Filename: " + filename)
                $.Topic(Logscape.Explorer.Topics.getFileContent).publish(filename)
            }
        } catch (err) {
            console.log(err.stack)
        }
        return false;
    })

    $.Topic(Logscape.Explorer.Topics.setListFiles).subscribe(function (listing) {
        setListing(listing)
    })
    function setListing(listing) {
        dataTable.fnClearTable()
        if (listing.length >0) {
            jQuery.each(listing, function (i, item) {
                item.volume = 0
                if (item.size > 2048) {
                    item.size = Number(item.size/1024).toLocaleString()  + "Kb"
                } else {
                    item.size = Number(item.size).toLocaleString()  + "b"
                }

                item.from = new Date(item.fromTime).toLocaleString();
                item.to =  new Date(item.toTime).toLocaleString();
                item.actions =
                    "<a class='fas fa-eye btn btn-link explorerFileActions view' data-action='view' data-filename='" + item.filename + "' href='#' title='View'></a>"+
                    "<a class='fas fa-search btn btn-link explorerFileActions' data-action='search' data-filename='" + item.filename + "' href='#' title='Search against this'></a>"+
                    "<a class='fas fa-times btn btn-link explorerFileActions' data-action='delete' data-filename='" + item.filename + "' href='#' title='Delete'></a>"+
                    "<a class='fas fa-cloud-download-alt btn btn-link explorerFileActions download' data-action='download' data-filename='" + item.filename + "' href='#' title='Download'></a> "
            })
            dataTable.fnAddData(listing)
            sources = listing.files
        }
    }

    function refreshIt() {
        $.Topic(Logscape.Explorer.Topics).publish("")
    }

    return {
        refresh: function () {
            refreshIt()
        }
    }

}