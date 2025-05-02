"""helpers for pulling the Community Features list from Cornell housing pages"""
from __future__ import annotations
import re, requests
from bs4 import BeautifulSoup

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

    # 1. Find a heading that matches our known set
    heading = None
    for tag in soup.find_all(re.compile("^h[1-6]$")):
        if any(h.lower() == tag.get_text(strip=True).lower() for h in HEADINGS):
            heading = tag
            break
    if not heading:
        return []

    # 2. The next <ul> or <p> after the heading contains the bullets
    section = heading.find_next(["ul", "p"])
    if not section:
        return []

    # 3. Extract bullet points
    if section.name == "ul":
        raw_items = [li.get_text(strip=True) for li in section.find_all("li")]
    else:  # <p>
        raw_items = [line.strip() for line in section.get_text("\n", strip=True).splitlines()]

    # 4. De‑dupe while preserving order
    deduped = list(dict.fromkeys(filter(None, raw_items)))
    return deduped


# Testing Blob
if __name__ == "__main__":
    demo = scrape_community_features(
        "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/mews-hall"
    )
    print(f"Found {len(demo)} items:\n  • " + "\n  • ".join(demo))
