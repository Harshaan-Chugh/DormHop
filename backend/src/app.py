"""
fetch_dorm_features.py
----------------------
Scrape the “Community Features” section of each Cornell dorm’s
housing page and emit a JSON mapping:

    { "Dorm Name": [feature1, feature2, ...], ... }

Requires:
    pip install requests beautifulsoup4
"""

from __future__ import annotations
import json, sys
from pathlib import Path

from scraper import scrape_community_features   # ← your existing helper


DORM_URLS: dict[str, str] = {
    "Alice Cook House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/alice-cook-house",
    "Balch Hall":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/balch-hall",
    "Barbara McClintock Hall":  "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/barbara-mcclintock-hall",
    "Carl Becker House":        "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/carl-becker-house",
    "Cascadilla Hall":          "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/residence-halls/cascadilla-hall",
    "Clara Dickson Hall":       "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/clara-dickson-hall",
    "Court–Kay–Bauer Hall":     "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/court-kay-bauer-hall",
    "Flora Rose House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/flora-rose-house",
    "Hans Bethe House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/hans-bethe-house",
    "High Rise 5":              "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/high-rise-5",
    "Hu Shih Hall":             "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/hu-shih-hall",
    "Jameson Hall":             "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/jameson-hall",
    "Low Rise 6":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/low-rise-6",
    "Low Rise 7":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/low-rise-7",
    "Mary Donlon Hall":         "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/mary-donlon-hall",
    "Mews Hall":                "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/mews-hall",
    "Ruth Bader Ginsburg Hall": "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/ruth-bader-ginsburg-hall",
    "William Keeton House":     "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/william-keeton-house",
}

def fetch_all() -> dict[str, list[str]]:
    out: dict[str, list[str]] = {}
    for dorm, url in DORM_URLS.items():
        try:
            feats = scrape_community_features(url)
            print(f"✅  {dorm:25} … {len(feats):2} features")
        except Exception as exc:
            print(f"⚠️   {dorm:25} … FAILED ({exc})")
            feats = []
        out[dorm] = feats
    return out


def main(dest: str | None = None) -> None:
    data = fetch_all()
    if dest:
        Path(dest).write_text(json.dumps(data, indent=2))
        print(f"\n💾  Wrote JSON to {dest}")
    else:
        print("\n" + json.dumps(data, indent=2))


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else None)
