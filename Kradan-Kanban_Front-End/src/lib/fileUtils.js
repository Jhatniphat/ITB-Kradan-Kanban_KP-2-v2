export function generateFileData(file, error = []) {
    return new Promise(async (resolve, reject) => {
        try {
            if (file.fileData && file.fileType) {
                // กรณีที่ไฟล์มาในรูปแบบ Base64
                const byteCharacters = atob(file.fileData);
                const byteNumbers = Array.from(byteCharacters, (char) => char.charCodeAt(0));
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], { type: file.fileType });


                const previewUrl =
                    file.type === "text/plain"
                        ? await createBlobWithUtf8(blob)
                        : URL.createObjectURL(blob);

                const fileData = {
                    id: file.id,
                    name: file.fileName,
                    size: blob.size,
                    type: file.fileType,
                    previewUrl,
                    thumbnail: file.fileType.startsWith("image/")
                        ? previewUrl
                        : file.fileType === "application/pdf"
                            ? await generatePDFThumbnail(blob)
                            : null,
                    errorText: error,
                };

                resolve(fileData);
            } else {
                // กรณีที่ไฟล์มาในรูปแบบ `File` object
                const reader = new FileReader();

                reader.onload = async (e) => {
                    try {
                        const previewUrl =
                            file.type === "text/plain"
                                ? await createBlobWithUtf8(file)
                                : URL.createObjectURL(file);

                        const fileData = {
                            name: file.name,
                            size: file.size,
                            type: file.type,
                            previewUrl,
                            thumbnail: file.type.startsWith("image/")
                                ? URL.createObjectURL(file)
                                : file.type === "application/pdf"
                                    ? await generatePDFThumbnail(file)
                                    : null,
                            errorText: error,
                        };

                        resolve(fileData);
                    } catch (err) {
                        reject(err); // ส่งข้อผิดพลาดหากเกิดปัญหา
                    }
                };

                reader.onerror = (err) => reject(err);

                // อ่านไฟล์ด้วย FileReader
                if (file.type === "text/plain") {
                    reader.readAsText(file, "utf-8");
                } else {
                    reader.readAsDataURL(file);
                }
            }
        } catch (generalError) {
            reject(generalError);
        }
    });
}



const generatePDFThumbnail = async (file) => {
    const pdf = await pdfjsLib.getDocument(URL.createObjectURL(file)).promise; // โหลด PDF
    const page = await pdf.getPage(1); // ดึงหน้าแรกของ PDF
    const viewport = page.getViewport({ scale: 1 }); // ตั้งค่า scale
    const canvas = document.createElement('canvas'); // สร้าง canvas
    const context = canvas.getContext('2d');

    // ตั้งขนาด canvas
    canvas.width = viewport.width;
    canvas.height = viewport.height;

    // เรนเดอร์ PDF บน canvas
    await page.render({
        canvasContext: context,
        viewport,
    }).promise;

    // แปลง canvas เป็น Blob URL
    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            const blobURL = URL.createObjectURL(blob);
            resolve(blobURL); // ส่ง Blob URL กลับ
        }, 'image/png'); // ใช้ PNG เป็นฟอร์แมต
    });
};

export function openPreview(file) {
    console.log(file.previewUrl);
    const downloadTypes = [
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
        "application/msword", // DOC , RTF
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
        "application/vnd.ms-excel", // XLS
        // "text/plain", // TXT
    ];

    if (downloadTypes.includes(file.type)) {
        // สร้างลิงก์สำหรับดาวน์โหลดไฟล์ แล้วกดลิงก์นั้น
        const link = document.createElement("a");
        link.href = file.previewUrl;
        link.download = file.name;
        link.click();
    } else {
        // เปิดไฟล์ในแท็บใหม่
        window.open(file.previewUrl, "_blank");
    }

};

const createBlobWithUtf8 = (file, sourceEncoding = "utf-8") => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = (e) => {
            try {
                // อ่านไฟล์เป็น ArrayBuffer
                const arrayBuffer = e.target.result;

                // แปลงข้อมูลจาก encoding ต้นทาง (sourceEncoding) ไปเป็น DOMString (UTF-16)
                const decoder = new TextDecoder(sourceEncoding);
                const text = decoder.decode(new Uint8Array(arrayBuffer)); // ถอดรหัสข้อมูล

                // สร้าง Blob ใหม่ใน UTF-8
                const utf8Blob = new Blob([text], { type: file.type || "text/plain;charset=utf-8" });
                const blobUrl = URL.createObjectURL(utf8Blob); // สร้าง Blob URL
                resolve(blobUrl); // ส่ง Blob URL กลับ
            } catch (error) {
                reject(error); // แจ้งข้อผิดพลาด
            }
        };

        reader.onerror = (err) => reject(err);

        // อ่านไฟล์เป็น ArrayBuffer เพื่อให้สามารถใช้ TextDecoder
        reader.readAsArrayBuffer(file);
    });
};