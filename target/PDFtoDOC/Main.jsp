<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ page
contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<c:if test="${empty sessionScope.UserID}"><c:redirect url="Login.jsp" /></c:if>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>Convert File</title>
    <link rel="stylesheet" href="./Main.css" />
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.1/css/all.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
      $(document).ready(function () {
        $("#convertForm").on("submit", function (e) {
          e.preventDefault();
          var formData = new FormData(this);
          var files = $("#pdfFiles").get(0).files;

          if (files.length === 0) {
            alert("Vui lòng chọn ít nhất một file PDF.");
            return;
          }

          var totalSize = 0;
          for (var i = 0; i < files.length; i++) {
            totalSize += files[i].size;
          }
          var totalSizeMB = (totalSize / (1024 * 1024)).toFixed(2);

          if (totalSize > 50 * 1024 * 1024) {
            alert(
              "Tổng kích thước các file vượt quá 50MB. Vui lòng chọn lại.\nTổng kích thước hiện tại: " +
                totalSizeMB +
                "MB"
            );
            return;
          }

          for (var i = 0; i < files.length; i++) {
            if (files[i].size > 30 * 1024 * 1024) {
              alert(
                'File "' +
                  files[i].name +
                  '" vượt quá 30MB. Vui lòng chọn file nhỏ hơn.'
              );
              return;
            }
          }

          $("#convertButton")
            .text("Đang tải lên " + files.length + " file...")
            .prop("disabled", true);

          $.ajax({
            url: "PdfController",
            type: "POST",
            data: formData,
            contentType: false,
            processData: false,
            success: function () {
              alert(
                "Đã tải lên thành công " +
                  files.length +
                  " file. Đang chuyển hướng..."
              );
              window.location.href = "HistoryController";
            },
            error: function (xhr, status, error) {
              var errorMsg = "Lỗi khi tải lên file.";
              if (xhr.status === 429) {
                errorMsg =
                  xhr.responseText ||
                  "Bạn đã vượt quá giới hạn (5 tasks/phút). Vui lòng thử lại sau.";
              } else if (xhr.status === 503) {
                errorMsg =
                  "Hàng đợi đầy hoặc file quá lớn. Vui lòng thử lại sau.";
              } else if (xhr.responseText) {
                errorMsg = xhr.responseText;
              }
              alert(errorMsg);
              $("#convertButton").text("Chuyển đổi").prop("disabled", false);
            },
          });
        });
      });
    </script>
  </head>
  <body>
    <div class="taskbar">
      <div class="taskbar__item">
        <ol>
          <li>
            <a href="./Main.jsp"
              ><i class="fa-solid fa-file-arrow-up"></i> Convert</a
            >
          </li>
          <li>
            <a href="HistoryController"
              ><i class="fa-solid fa-list-check"></i> History</a
            >
          </li>
        </ol>
        <div class="account">
          <i class="fa-solid fa-user-circle"></i> ${sessionScope.Username}
          <a
            href="LogoutController"
            style="text-decoration: none; color: #007bff; margin-left: 10px"
            ><i class="fa-solid fa-right-from-bracket"></i> Logout</a
          >
        </div>
      </div>
    </div>
    <div class="Converter">
      <h1>PDF to Docx Converter</h1>
      <div class="Converter_item">
        <form
          id="convertForm"
          action="PdfController"
          method="post"
          enctype="multipart/form-data"
        >
          <div class="Content">
            <i class="fa-solid fa-cloud-arrow-up"></i>

            <h3 style="color: #555; margin: 0">Tải lên tài liệu PDF</h3>
            <p style="color: #999; font-size: 14px; margin-top: 5px">
              Bạn có thể chọn nhiều file PDF cùng lúc (tối đa 50MB tổng cộng)
            </p>

            <div class="upload-wrapper" style="margin: 20px 0">
              <input
                type="file"
                id="pdfFiles"
                name="pdfFile"
                accept=".pdf"
                multiple
                required
                onchange="showFileName()"
              />
              <label for="pdfFiles" class="custom-file-upload">
                <i class="fa-solid fa-folder-open"></i> Chọn File từ máy tính
                (có thể chọn nhiều file)
              </label>
            </div>

            <div
              id="fileNameDisplay"
              style="
                color: #a18cd1;
                font-weight: 600;
                margin-bottom: 10px;
                font-size: 14px;
                max-height: 200px;
                overflow-y: auto;
                padding: 10px;
                background: #f8f9fa;
                border-radius: 5px;
              "
            ></div>
            <div
              id="fileCountDisplay"
              style="color: #666; font-size: 12px; margin-bottom: 10px"
            ></div>

            <button type="submit" id="convertButton">Chuyển đổi</button>
          </div>

          <script>
            function showFileName() {
              const input = document.getElementById("pdfFiles");
              const display = document.getElementById("fileNameDisplay");
              const countDisplay = document.getElementById("fileCountDisplay");

              if (input.files.length > 0) {
                let fileNames = "";
                let totalSize = 0;

                for (let i = 0; i < input.files.length; i++) {
                  const file = input.files[i];
                  const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
                  totalSize += file.size;

                  const isTooLarge = file.size > 30 * 1024 * 1024;
                  const warningIcon = isTooLarge
                    ? ' <i class="fa-solid fa-triangle-exclamation" style="color: red;" title="File vượt quá 30MB"></i>'
                    : "";

                  fileNames += '<div style="margin-bottom: 5px;">';
                  fileNames +=
                    '<i class="fa-regular fa-file-pdf" style="color: #dc3545;"></i> ';
                  fileNames +=
                    '<span style="' +
                    (isTooLarge ? "color: red; font-weight: bold;" : "") +
                    '">' +
                    file.name +
                    "</span>";
                  fileNames +=
                    ' <span style="color: #666; font-size: 12px;">(' +
                    fileSizeMB +
                    " MB)</span>";
                  fileNames += warningIcon;
                  fileNames += "</div>";
                }

                display.innerHTML = fileNames;

                const totalSizeMB = (totalSize / (1024 * 1024)).toFixed(2);
                const isTotalTooLarge = totalSize > 50 * 1024 * 1024;
                countDisplay.innerHTML =
                  '<i class="fa-solid fa-info-circle"></i> Đã chọn <strong>' +
                  input.files.length +
                  "</strong> file" +
                  (input.files.length > 1 ? "s" : "") +
                  ' | Tổng kích thước: <strong style="' +
                  (isTotalTooLarge ? "color: red;" : "") +
                  '">' +
                  totalSizeMB +
                  " MB</strong>" +
                  (isTotalTooLarge
                    ? ' <span style="color: red;">(Vượt quá 50MB!)</span>'
                    : "");
              } else {
                display.innerHTML = "";
                countDisplay.innerHTML = "";
              }
            }
          </script>
        </form>
      </div>
    </div>
  </body>
</html>
