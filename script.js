async function encryptMessage() {
  const message = document.getElementById("inputText").value.trim();
  if (!message) {
    showNotification("Por favor, ingresa un mensaje para cifrar.", true);
    return;
  }

  try {
    const response = await fetch("http://localhost:8080/api/encryptClickRate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(message),
    });

    if (!response.ok) throw new Error("Error al cifrar el mensaje.");
    const encryptedMessage = await response.text();
    document.getElementById("encryptedResult").innerText = encryptedMessage;
    document.getElementById("encryptedLabel").classList.remove("hidden");
    document.getElementById("encryptedResult").classList.remove("hidden");
    showNotification("Mensaje cifrado con éxito.");
  } catch (error) {
    showNotification(error.message, true);
  }
}

async function decryptMessage() {
  const hexMessage = document.getElementById("inputCipherText").value.trim();
  if (!hexMessage) {
    showNotification("Por favor, ingresa un mensaje cifrado para descifrar.", true);
    return;
  }

  try {
    const response = await fetch("http://localhost:8080/api/decryptClickRate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(hexMessage),
    });

    if (!response.ok) throw new Error("Error al descifrar el mensaje.");
    const decryptedMessage = await response.text();
    document.getElementById("decryptedResult").innerText = decryptedMessage;
    document.getElementById("decryptedLabel").classList.remove("hidden");
    document.getElementById("decryptedResult").classList.remove("hidden");
    showNotification("Mensaje descifrado con éxito.");
  } catch (error) {
    showNotification(error.message, true);
  }
}

function showNotification(message, isError = false) {
  const notification = document.getElementById("notification");
  notification.textContent = message;
  notification.className = `notification ${isError ? 'error' : ''} fade-in`;
    setTimeout(() => {
    notification.className = "notification fade-out";
    setTimeout(() => {
      notification.textContent = "";
      notification.classList.add("hidden");
    }, 500);
  }, 3000);
}
