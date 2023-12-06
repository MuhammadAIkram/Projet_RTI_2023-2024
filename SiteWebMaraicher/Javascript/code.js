const table = document.getElementById('myTable');
const tableBody = document.getElementById('tableBody');
const ArticleForm = document.getElementById('FormArticle');
const formInputs = ArticleForm.getElementsByTagName('input');
let selectedRow = null;

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
            changeForm(item.id, item.intitule, item.prix, item.stock);
            selectedRow = item;
        });
        tableBody.appendChild(newRow);
    });
}

function changeForm(id, article, prix, quant){
    document.getElementById("ID_Art").value = id;
    document.getElementById("Nom_Art").value = article;
    prix = parseFloat(prix);
    document.getElementById("Prix_Art").value = prix.toFixed(2);
    document.getElementById("Quant_Art").value = quant;
}

function changeImage(img) {
    document.getElementById("ArticleImage").src = `images/${img}`;
}

ArticleForm.addEventListener('submit', function(event){
    event.preventDefault();
    if(selectedRow){
        selectedRow.prix = formInputs[2].value;
        selectedRow.stock = formInputs[3].value;

        console.log(selectedRow);

        fetch('/FormArticle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(selectedRow)
        })
        .then(response => {
            if (response.ok) {
                alert("les données ont été mises à jour");

                videTable();

                selectedRow = null;
                formInputs[0].value = '';
                formInputs[1].value = '';
                formInputs[2].value = '';
                formInputs[3].value = '';
                document.getElementById("ArticleImage").src = '';

                fetch('Fichiers/articles.json')
                    .then(response => response.json())
                    .then(data => {
                        createRowsFromJSON(data);
                    })
                    .catch(error => {
                        console.error('Error fetching data:', error);
                    });
            } else {
                alert("Erreur mises à jour");
                // Handle error cases here
            }
        })
        .catch(error => {
            console.error('Error:', error);
            // Handle network errors or exceptions here
        });
    }
    else alert("veuillez sélectionner un article");
});

function videTable()
{
    while (table.rows.length > 1) {
        table.deleteRow(-1); // supprimer dernière ligne
    }
}

fetch('Fichiers/articles.json')
    .then(response => response.json())
    .then(data => {
        createRowsFromJSON(data);
    })
    .catch(error => {
        console.error('Error fetching data:', error);
    });
