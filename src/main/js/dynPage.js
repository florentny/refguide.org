//
// (c) 2007-2026 Florent Charpin
//  www@reefguide.org
//

const SIZE_CONFIGS = {
    "0": { imgWidth: 160, imgWidthAct: 144, thumbClass: "thumb3", cellClass: "celltdsmall" },
    "1": { imgWidth: 240, imgWidthAct: 216, thumbClass: "thumb",  cellClass: "celltd" },
    "2": { imgWidth: 400, imgWidthAct: 370, thumbClass: "thumb2", cellClass: "celltdbig" },
    "3": { imgWidth: 316, imgWidthAct: 293, thumbClass: "thumb4", cellClass: "celltdbig1" }
};

var img_width = 240;
let currentConfig = SIZE_CONFIGS["1"];
let curCol = 0;
let cookieChecked = false;

function setCookie(name, value) {
    document.cookie = `${name}=${encodeURIComponent(value)};path=/`;
}

function getCookie(name) {
    const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
    return match ? decodeURIComponent(match[1]) : null;
}

function setSize(sizeKey) {
    currentConfig = SIZE_CONFIGS[sizeKey];
    img_width = currentConfig.imgWidth;
    curCol = 0;
    setCookie("Reefsize", sizeKey);
    creategrid();
}

function sizesmall() { setSize("0"); }
function sizereg()   { setSize("1"); }
function sizebig()   { setSize("2"); }
function sizebig1()  { setSize("3"); }

function creategrid() {
    const viewWidth = window.innerWidth;

    if (!cookieChecked) {
        let cookie = getCookie("Reefsize");
        if (cookie === null) {
            if (viewWidth < 920) cookie = "0";
            else if (viewWidth > 2200) cookie = "3";
        }
        if (cookie !== null && SIZE_CONFIGS[cookie]) {
            currentConfig = SIZE_CONFIGS[cookie];
            img_width = currentConfig.imgWidth;
            curCol = 0;
        }
        cookieChecked = true;
    }

    const { imgWidth, imgWidthAct, thumbClass, cellClass } = currentConfig;
    const numpix = img_reef.length;
    let numCol = Math.floor((viewWidth - panelOffset) / imgWidth);
    if (maxCol !== 0 && numCol > maxCol) numCol = maxCol;
    if (curCol === numCol) return;
    curCol = numCol;

    const container = document.getElementById("TopTable");
    container.innerHTML = "";

    let colpos = 0;
    let from = 0;
    let catIndex = 0;
    let imageRow, nameRow;

    for (let i = 0; i < numpix; i++) {
        // At the start of each row, insert category headers
        if (colpos === 0) {
            const headerRow = document.createElement("div");
            headerRow.className = "grid-row";
            headerRow.style.gridTemplateColumns = `repeat(${numCol}, 1fr)`;

            const end = Math.min(i + numCol, numpix);
            let j = i;

            while (j < end) {
                let span = 1;
                let dispHeader = false;
                let newCat = false;

                if (i !== 0 && j === i && catIndex > 0) {
                    if (ref_reef[catIndex] < end) {
                        dispHeader = true;
                        span = ref_reef[catIndex] - j;
                    } else if (ref_reef[catIndex - 1] > (j - numCol)) {
                        dispHeader = true;
                        span = Math.min(numCol, end - j);
                    }
                }

                if (ref_reef[catIndex] === j) {
                    dispHeader = true;
                    newCat = true;
                    span = (ref_reef[catIndex + 1] >= i + numCol)
                        ? i + numCol - j
                        : ref_reef[catIndex + 1] - j;
                }

                if (dispHeader) {
                    const cell = document.createElement("div");
                    cell.className = "catrow";
                    cell.style.gridColumn = `span ${span}`;

                    const catDiv = document.createElement("div");
                    catDiv.className = "catheader";

                    const catName = newCat ? cat_reef[catIndex] : cat_reef[catIndex - 1];
                    if (newCat && i !== 0) {
                        catDiv.id = catName.replace(/ /g, "_");
                    }

                    const link = document.createElement("a");
                    link.href = catName.replace(/ /g, "_") + ".html";
                    link.textContent = catName;
                    catDiv.appendChild(link);
                    cell.appendChild(catDiv);
                    headerRow.appendChild(cell);

                    if (newCat) catIndex++;
                    j += span;
                } else {
                    const cell = document.createElement("div");
                    cell.className = "catrow";
                    cell.style.gridColumn = `span ${span}`;
                    headerRow.appendChild(cell);
                    j += span;
                }
            }

            container.appendChild(headerRow);

            imageRow = document.createElement("div");
            imageRow.className = "grid-row";
            imageRow.style.gridTemplateColumns = `repeat(${numCol}, 1fr)`;

            nameRow = document.createElement("div");
            nameRow.className = "grid-row";
            nameRow.style.gridTemplateColumns = `repeat(${numCol}, 1fr)`;
        }

        const imgCell = document.createElement("div");
        imgCell.className = cellClass;

        const imgLink = document.createElement("a");
        imgLink.href = link_reef[i];

        const img = document.createElement("img");
        img.className = "selframe";
        img.src = img_reef[i].replace("thumb", thumbClass);
        img.width = imgWidthAct;
        img.alt = name_reef[i] + (name_sci[i] ? " - " + name_sci[i] : "");
        img.title = name_reef[i] + (name_sci[i] ? " - " + name_sci[i] : "");

        imgLink.appendChild(img);
        imgCell.appendChild(imgLink);
        imageRow.appendChild(imgCell);

        colpos++;
        if (colpos === numCol || i === numpix - 1) {
            container.appendChild(imageRow);

            for (let j = from; j <= i; j++) {
                const nameCell = document.createElement("div");
                nameCell.className = "nameid";
                nameCell.style.width = imgWidth + "px";

                const nameLink = document.createElement("a");
                nameLink.className = "nameid";
                nameLink.href = link_reef[j];
                nameLink.textContent = name_reef[j];

                const nameDiv = document.createElement("div");
                nameDiv.className = "nameid";
                nameDiv.appendChild(nameLink);
                nameCell.appendChild(nameDiv);
                nameRow.appendChild(nameCell);
            }
            container.appendChild(nameRow);

            from = i + 1;
            colpos = 0;
        }
    }
}

function hideLevel(id) {
    const el = document.getElementById(id);
    el.style.display = "none";
    document.getElementById("tree" + id).className = "close";
}

function show(id) {
    const el = document.getElementById(id);
    if (el.style.display === "none") {
        el.style.display = "block";
        document.getElementById("tree" + id).className = "open";
    } else {
        hideLevel(id);
    }
}
