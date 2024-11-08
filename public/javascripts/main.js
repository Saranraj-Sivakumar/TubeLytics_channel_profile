function search() {
    const query = document.getElementById('searchQuery').value;
    const encodedQuery = encodeURIComponent(query);

    fetch(`/tubelytics/search?query=` + encodedQuery)
        .then(response => response.json())
        .then(data => {
            let resultsDiv = document.getElementById("results");
            resultsDiv.innerHTML = '';

            // Retrieve the average scores from the response
            let avgFkGrade = data.avgFleschKincaidGrade || "##";
            let avgReadingEase = data.avgFleschReadingEase || "##";

            // Display the search header with the average readability scores
            let queryDiv = document.createElement("div");
            queryDiv.className = "search-header";
            queryDiv.innerHTML = `
                <p>Search terms: ${query} <span class="sentiment">:-|</span> (Flesch-Kincaid Grade Level Avg. = ${avgFkGrade}, Flesch Reading Ease Score Avg. = ${avgReadingEase}) <a href="#" class="more-stats"> More stats</a></p>
            `;

            // Loop over each set of video results
            data.items.forEach((item, index) => {
                let fkGrade = item.snippet.fkGrade || "##";
                let readingEase = item.snippet.readingEase || "##";

                let video = document.createElement("div");
                video.className = "video-result";
                video.innerHTML = `
                    <div class="video-content">
                        <p class="video-title">${index + 1}. Title: <a href="https://www.youtube.com/watch?v=${item.id.videoId}" target="_blank">${item.snippet.title}</a></p>
                        <p><strong>Channel:</strong> <a href="/channel/${item.snippet.channelId}" target="_self">${item.snippet.channelTitle}</a></p>
                        <p><strong>Description:</strong> "${item.snippet.description}"</p>
                        <p class="readability-score">Flesch-Kincaid Grade Level = ${fkGrade}, Flesch Reading Ease Score = ${readingEase}</p>
                        <p><a href="#" class="tags-link">Tags</a></p>
                    </div>
                    <div class="video-thumbnail">
                        <img src="${item.snippet.thumbnails.default.url}" alt="Thumbnail" class="thumbnail">
                    </div>
                `;

                queryDiv.appendChild(video);
            });

            // Append each query's result set to the results div
            resultsDiv.appendChild(queryDiv);
        })
        .catch(error => console.error("Error fetching search results:", error));
}
