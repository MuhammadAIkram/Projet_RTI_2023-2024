const table = document.getElementById('myTable');
const tableBody = document.getElementById('tableBody');

function createRowsFromJSON(data) {

    data.forEach(item => {
        const newRow = document.createElement('tr');
        newRow.innerHTML = `
          <td>${item.id}</td>
          <td>${item.intitule}</td>
          <td>${item.prix.toFixed(2)}</td>
          <td>${item.stock}</td>
        `;

        newRow.addEventListener('click', function() {
            const selected = table.getElementsByClassName('selected');
            for (let j = 0; j < selected.length; j++) {
                selected[j].classList.remove('selected');
            }
            this.classList.add('selected');
            changeImage(item.image);
            changeForm(item.id, item.intitule, item.prix.toFixed(2), item.stock);
        });
        tableBody.appendChild(newRow);
    });
}

function changeForm(id, article, prix, quant){
    document.getElementById("ID_Art").value = id;
    document.getElementById("Nom_Art").value = article;
    document.getElementById("Prix_Art").value = prix;
    document.getElementById("Quant_Art").value = quant;
}

function changeImage(img) {
    document.getElementById("ArticleImage").src = `images/${img}`;
}

fetch('Fichiers/articles.json')
    .then(response => response.json())
    .then(data => {
        createRowsFromJSON(data);
    })
    .catch(error => {
        console.error('Error fetching data:', error);
    });
