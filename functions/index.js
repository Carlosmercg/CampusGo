/* eslint-disable indent, object-curly-spacing, comma-dangle, key-spacing, max-len */
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.enviarNotificacionChat = functions.database
  .ref("/chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const nuevoMensaje = snapshot.val();
    if (!nuevoMensaje) {
      return null;
    }

    const chatId = context.params.chatId;
    const contenido = nuevoMensaje.contenido || "";
    const uidEmisor = nuevoMensaje.emisor || "";
    const uidReceptor = nuevoMensaje.receptor || "";

    // 1) Obtener nombre del emisor desde Firestore
    let nombreEmisor = "Alguien";
    try {
      const docEmisor = await admin
        .firestore()
        .collection("usuarios")
        .doc(uidEmisor)
        .get();
      if (docEmisor.exists && docEmisor.data().nombre) {
        nombreEmisor = docEmisor.data().nombre;
      }
    } catch (error) {
      console.error("enviarNotificacionChat → Error obteniendo nombreEmisor:", error);
    }

    // 2) Obtener token FCM del receptor desde Firestore
    let tokenReceptor = null;
    try {
      const docReceptor = await admin
        .firestore()
        .collection("usuarios")
        .doc(uidReceptor)
        .get();
      if (docReceptor.exists && docReceptor.data().fcmToken) {
        tokenReceptor = docReceptor.data().fcmToken;
      }
    } catch (error) {
      console.error("enviarNotificacionChat → Error obteniendo fcmToken:", error);
    }

    if (!tokenReceptor) {
      console.log("enviarNotificacionChat → No hay token FCM para UID:", uidReceptor);
      return null;
    }

    // 3) Construir payload del Data Message para FCM
    const mensajeFCM = {
      token: tokenReceptor,
      data: {
        titulo: `${nombreEmisor} te ha enviado un mensaje`,
        cuerpo: contenido,
        nombreEmisor: nombreEmisor,
        ultimoMensaje: contenido,
        chatId: chatId,
        uidEmisor: uidEmisor,
      },
      android: {
        priority: "high",
      },
      apns: {
        headers: {
          "apns-priority": "10",
        },
        payload: {
          aps: {
            sound: "default",
          },
        },
      },
    };

    // 4) Enviar la notificación vía Admin SDK
    try {
      const response = await admin.messaging().send(mensajeFCM);
      console.log("enviarNotificacionChat → Notificación enviada:", response);
    } catch (error) {
      console.error("enviarNotificacionChat → Error enviando notificación:", error);
    }

    return null;
  });
