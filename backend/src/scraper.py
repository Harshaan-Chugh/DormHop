from __future__ import annotations
import re
import requests
from bs4 import BeautifulSoup
from urls import DORM_URLS

HEADERS = {
    "User-Agent": "Mozilla/5.0 (DormHop internal scraper)"
}

HEADINGS = {
    "Community Features",
    "Community features",
    "Amenities",
    "Community amenities",
}

def scrape_community_features(url: str, timeout: int = 10) -> list[str]:
    """
    Return a list of features (strings) pulled from a Cornell dorm page.
    """
    resp = requests.get(url, headers=HEADERS, timeout=timeout)
    resp.raise_for_status()

    soup = BeautifulSoup(resp.text, "html.parser")

    # 1. Find a heading that matches (ignoring trailing colons & case)
    heading = None
    for tag in soup.find_all(re.compile(r"^h[1-6]$")):
        text = tag.get_text(strip=True).rstrip(":").lower()
        if any(text == h.lower() for h in HEADINGS):
            heading = tag
            break
    if not heading:
        return []

    # 2. Look for the first <p> or <ul> after that heading
    section = heading.find_next(["p", "ul"])
    if not section:
        return []

    raw_items: list[str] = []

    if section.name == "p":
        # 3a. Grab each line from the paragraph
        raw_items.extend(
            line.strip()
            for line in section.get_text("\n", strip=True).splitlines()
            if line.strip()
        )
        # 3b. If there's a UL immediately after it, grab those too
        ul = section.find_next_sibling("ul")
        if ul:
            raw_items.extend(li.get_text(strip=True) for li in ul.find_all("li"))
    else:
        # 4. Straight‐up list
        raw_items = [li.get_text(strip=True) for li in section.find_all("li")]

    # 5. De-dupe while preserving order
    return list(dict.fromkeys(raw_items))

# Testing Blob: iterate through all dorm URLs and output results
if __name__ == "__main__":
    for dorm_name, dorm_url in DORM_URLS.items():
        print(f"===> {dorm_name}")
        try:
            features = scrape_community_features(dorm_url)
            print(f"Found {len(features)} features:")
            for feat in features:
                print(f"  • {feat}")
        except Exception as exc:
            print(f"Error fetching {dorm_name}: {exc}")
        print()
