<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:if test="${empty sessionScope.UserID}"><c:redirect url="Login.jsp" /></c:if>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Convert File</title>
    <link rel="stylesheet" href="./Main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.1/css/all.min.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $('#convertForm').on('submit', function (e) {
                e.preventDefault();
                var formData = new FormData(this);
                if ($('#pdfFiles').get(0).files.length === 0) { alert('Chưa chọn file.'); return; }
                $('#convertButton').text('Đang tải lên...').prop('disabled', true);
                $.ajax({
                    url: 'PdfController', type: 'POST', data: formData,
                    contentType: false, processData: false,
                    success: function () { window.location.href = 'HistoryController'; },
                    error: function (xhr, status, error) {
                        alert('Lỗi: ' + error);
                        $('#convertButton').text('Convert and Merge').prop('disabled', false);
                    }
                });
            });
        });
    </script>
</head>
<body>
    <div class="taskbar">
        <div class="taskbar__item">
            <ol>
                <li><a href="./Main.jsp"><i class="fa-solid fa-file-arrow-up"></i> Convert</a></li>
                <li><a href="HistoryController"><i class="fa-solid fa-list-check"></i> History</a></li>
            </ol>
            <div class="account">
                <i class="fa-solid fa-user-circle"></i> ${sessionScope.Username}
                <a href="LogoutController" style="text-decoration: none; color: #007bff; margin-left: 10px;"><i class="fa-solid fa-right-from-bracket"></i> Logout</a>
            </div>
        </div>
    </div>
    <div class="Converter">
        <h1>PDF to Docx Converter</h1>
        <div class="Converter_item">
            <form id="convertForm" action="PdfController" method="post" enctype="multipart/form-data">
<div class="Content">
    <i class="fa-solid fa-cloud-arrow-up"></i>
    
    <h3 style="color: #555; margin: 0;">Tải lên tài liệu PDF</h3>
    <p style="color: #999; font-size: 14px; margin-top: 5px;">Kéo thả file vào đây hoặc bấm nút bên dưới</p>

    <div class="upload-wrapper" style="margin: 20px 0;">
        <input type="file" id="pdfFiles" name="pdfFile" accept=".pdf" multiple required onchange="showFileName()">
        <label for="pdfFiles" class="custom-file-upload">
            <i class="fa-solid fa-folder-open"></i> Chọn File từ máy tính
        </label>
    </div>

    <div id="fileNameDisplay" style="color: #a18cd1; font-weight: 600; margin-bottom: 10px; font-size: 14px;"></div>

    <button type="submit" id="convertButton">Convert and Merge</button>
</div>

<script>
    function showFileName() {
        const input = document.getElementById('pdfFiles');
        const display = document.getElementById('fileNameDisplay');
        
        if (input.files.length > 0) {
            let fileNames = "";
            for (let i = 0; i < input.files.length; i++) {
                fileNames += '<i class="fa-regular fa-file-pdf"></i> ' + input.files[i].name + '<br>';
            }
            display.innerHTML = fileNames;
        } else {
            display.innerHTML = "";
        }
    }
</script>
            </form>
        </div>
    </div>
</body>
</html>