<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib
prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${empty sessionScope.UserID}">
  <c:redirect url="Login.jsp" />
</c:if>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>Lịch sử chuyển đổi</title>
    <link rel="stylesheet" href="./Main.css" />
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.1/css/all.min.css"
    />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/mammoth/1.6.0/mammoth.browser.min.js"></script>
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
          >
            <i class="fa-solid fa-right-from-bracket"></i> Logout
          </a>
        </div>
      </div>
    </div>

    <div class="History" style="display: flex">
      <h1>Lịch sử chuyển đổi</h1>
      <table id="historyTable">
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên file</th>
            <th>Thời gian</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody id="historyTableBody">
          <c:forEach var="history" items="${fileHistory}" varStatus="status">
            <tr>
              <td>${status.index + 1}</td>
              <td>${history.fileName}</td>
              <td>
                <fmt:formatDate
                  value="${history.dateConvert}"
                  pattern="HH:mm:ss dd/MM/yyyy"
                />
              </td>

              <td
                style="
                  color: $ {
                    history.status== 'Success' ? 'green' : (
                      history.status == 'Failed' ? 'red': '#E69B00'
                    );
                  }
                "
              >
                <c:choose>
                  <c:when test="${history.status == 'Success'}"
                    >✅ Success</c:when
                  >
                  <c:when test="${history.status == 'Failed'}"
                    >❌ Failed</c:when
                  >
                  <c:when test="${history.status == 'Processing'}"
                    >🔵 Processing...</c:when
                  >
                  <c:otherwise>🟡 Waiting</c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${history.status == 'Success'}">
                    <a
                      href="DownloadController?inforId=${history.inforID}"
                      title="Tải về"
                    >
                      <i class="fa-solid fa-download"></i>
                    </a>
                    &nbsp;&nbsp;
                    <a
                      href="#"
                      onclick="previewDoc(${history.inforID}, '${history.fileName}'); return false;"
                      title="Xem trước"
                    >
                      <i class="fa-solid fa-eye" style="color: #007bff"></i>
                    </a>
                  </c:when>
                  <c:otherwise>
                    <i class="fa-solid fa-circle-xmark"></i>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <div id="previewModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2 id="previewTitle">Xem trước tài liệu</h2>
          <span class="close" onclick="closeModal()">&times;</span>
        </div>
        <div class="modal-body" id="documentContainer">
          <p>Đang tải tài liệu...</p>
        </div>
      </div>
    </div>

    <script>
      function previewDoc(inforId, fileName) {
        const modal = document.getElementById("previewModal");
        const container = document.getElementById("documentContainer");
        const title = document.getElementById("previewTitle");

        modal.style.display = "flex";
        title.innerText = "Đang xem: " + fileName;
        container.innerHTML =
          '<div class="loading-spinner">Wait a moment...</div>';

        fetch("DownloadController?inforId=" + inforId)
          .then((response) => response.arrayBuffer())
          .then((arrayBuffer) => {
            mammoth
              .convertToHtml({ arrayBuffer: arrayBuffer })
              .then((result) => {
                container.innerHTML = result.value;
              })
              .catch((err) => {
                container.innerHTML =
                  '<p style="color:red">Không thể xem trước file này. Vui lòng tải về.</p>';
              });
          })
          .catch((err) => {
            container.innerHTML =
              '<p style="color:red">Lỗi kết nối server.</p>';
          });
      }

      function closeModal() {
        document.getElementById("previewModal").style.display = "none";
      }

      window.onclick = function (event) {
        const modal = document.getElementById("previewModal");
        if (event.target == modal) {
          modal.style.display = "none";
        }
      };

      (function () {
        let updateCount = 0;

        function escapeHtml(text) {
          if (!text) return "";
          const map = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;",
          };
          return text.replace(/[&<>"']/g, (m) => map[m]);
        }

        async function updateHistory() {
          try {
            updateCount++;
            const url = "ApiHistoryController?t=" + new Date().getTime();
            const res = await fetch(url, {
              method: "GET",
              credentials: "same-origin",
              headers: {
                Accept: "application/json",
              },
            });

            if (res.ok) {
              const tasks = await res.json();
              const tbody = document.getElementById("historyTableBody");

              if (!tbody) {
                console.error("[AUTO-REFRESH] Không tìm thấy tbody element");
                return;
              }

              let newHtml = "";

              tasks.forEach((t, i) => {
                const color =
                  t.status === "Success"
                    ? "green"
                    : t.status === "Failed"
                    ? "red"
                    : "#E69B00";
                let statusText =
                  t.status === "Success"
                    ? "✅ Success"
                    : t.status === "Processing"
                    ? "🔵 Processing..."
                    : "❌ Failed";

                let actions = '<i class="fa-solid fa-circle-xmark"></i>';
                if (t.status === "Success") {
                  actions =
                    `
                                    <a href="DownloadController?inforId=` +
                    t.inforID +
                    `" title="Tải về"><i class="fa-solid fa-download"></i></a>
                                    &nbsp;&nbsp;
                                    <a href="#" onclick="previewDoc(` +
                    t.inforID +
                    `, '` +
                    escapeHtml(t.fileName) +
                    `'); return false;" title="Xem trước"><i class="fa-solid fa-eye" style="color: #007bff;"></i></a>
                                `;
                } else if (t.status === "Processing") {
                  actions =
                    '<i class="fa-solid fa-spinner fa-spin" style="color: #E69B00"></i>';
                }

                let dateStr = "";
                if (t.dateConvert) {
                  try {
                    const date = new Date(t.dateConvert);
                    dateStr = date.toLocaleString("vi-VN", {
                      hour: "2-digit",
                      minute: "2-digit",
                      second: "2-digit",
                      day: "2-digit",
                      month: "2-digit",
                      year: "numeric",
                    });
                  } catch (e) {
                    dateStr = t.dateConvert;
                  }
                }

                newHtml +=
                  `<tr>
                                <td>` +
                  (i + 1) +
                  `</td>
                                <td>` +
                  escapeHtml(t.fileName || "") +
                  `</td>
                                <td>` +
                  dateStr +
                  `</td>
                                <td style="color:` +
                  color +
                  `">` +
                  statusText +
                  `</td>
                                <td>` +
                  actions +
                  `</td>
                            </tr>`;
              });

              tbody.innerHTML = newHtml;

              if (updateCount % 10 === 0) {
                console.log(
                  "[AUTO-REFRESH] Đã cập nhật " +
                    updateCount +
                    " lần. Số tasks: " +
                    tasks.length
                );
              }
            } else {
              console.error(
                "[AUTO-REFRESH] API error:",
                res.status,
                res.statusText
              );
              if (res.status === 401) {
                console.warn(
                  "[AUTO-REFRESH] Session expired, redirecting to login..."
                );
                window.location.href = "Login.jsp";
              }
            }
          } catch (e) {
            console.error("[AUTO-REFRESH] Error:", e);
          }
        }

        updateHistory();

        setInterval(updateHistory, 3000);

        console.log("[AUTO-REFRESH] Đã khởi động auto-refresh (mỗi 3 giây)");
      })();
    </script>
  </body>
</html>
