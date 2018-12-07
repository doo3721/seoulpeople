# Project Title

Like Seoul People

---


**Badges will go here**

- devDependencies
- issues (waffle.io maybe)
- license

[![Plat Form](https://img.shields.io/badge/Platform-Android-lightgrey.svg)
![AppVeyor](https://img.shields.io/appveyor/ci/:user/:repo.svg)](https://github.com/doo3721/seoulpeople)
[![GitHub issues](https://img.shields.io/github/issues/doo3721/seoulpeople.svg)](https://github.com/doo3721/seoulpeople)
![License: CC BY 4.0](https://img.shields.io/badge/license-CC%20BY%204.0%20%2F%20Apache--2.0-blue.svg)

---


## Getting Started

These instructions will get you a copy of the project up and running on your mobile device for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

---


### Clone

- Clone this repo to your local virtual mobile machine (also you can use your physical mobile phone) using [https://github.com/doo3721/seoulpeople](https://github.com/doo3721/seoulpeople "https://github.com/doo3721/seoulpeople")

---


## Usage

you can release apk file on your IDE, or just see the below video!

![build-test](https://media.giphy.com/media/5WkBtuHtdHE30mLSXt/giphy.gif)

in app, you can find like this method

```java
// Voice Recg
private DetectNoise mSensor;

// in recordactivity.java
float amp = (float) mSensor.getAmplitude();
                    amp = (float) ((amp - (-20.0)) / (12.0 - (-20.0)) * 100.0);
                    if (amp <= 0) amp = 0.0f;   if(amp >= 100) amp = 100.0f;
                    soundAmpList.add(amp);

// for sentence analyze
i_speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
i_speech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
i_speech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
i_speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
```

Data sava and load method in resultactivity.java
```sql
    private void init_tables() {
        // 테이블 처음 생성시 초기화
        if (sqliteDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CONTACT_T (" +
                    "numi " + "INTEGER NOT NULL," +
                    "savedate " + "TEXT NOT NULL," +
                    "AmpList " + "TEXT NOT NULL," +
                    "RmsList " + "TEXT NOT NULL," +
                    "sentenceC " + "TEXT NOT NULL" + ")";

            System.out.println(sqlCreateTbl);
            sqliteDB.execSQL(sqlCreateTbl);
        }
    }
```

We use sentence analyze with google diff_patch_match algorithms, which is
[https://github.com/google/diff-match-patch](https://github.com/google/diff-match-patch "https://github.com/google/diff-match-patch")

---


## Contributing

> To get started...

### Step 1

- **Option 1**
    - Fork this repo!

- **Option 2**
    - Clone this repo to your local machine using [https://github.com/doo3721/seoulpeople.git](https://github.com/doo3721/seoulpeople.git "https://github.com/doo3721/seoulpeople.git")

### Step 2

- **HACK AWAY!**

### Step 3

- Create a new pull request using [https://github.com/doo3721/seoulpeople/compare](https://github.com/doo3721/seoulpeople/compare "https://github.com/doo3721/seoulpeople/compare")

---


## Team

> Or Contributors/People

| HeeJin Lee | DooHee Kim | DoHyoung Lee | Sunghun Bak |
| :---: |:---:| :---:| :---: |
| <img src="https://avatars1.githubusercontent.com/u/9789023?s=460&v=4" width="70%"></img>    | <img src="https://avatars3.githubusercontent.com/u/34649424?s=400&v=4" width="70%"></img> |   |  <img src="https://avatars3.githubusercontent.com/u/34119627?s=400&v=4" width="100%"></img> |
| [github.com/Leeheejin](https://github.com/Leeheejin "https://github.com/Leeheejin") | [github.com/doo3721](https://github.com/doo3721 "https://github.com/doo3721") | [github.com/doh01](https://github.com/doh01 "https://github.com/doh01") | [github.com/sg03142](https://github.com/sg03142 "https://github.com/sg03142") |

- You can see team member and github profile
- You should probably find team member's lastest project

---


## FAQ

- **How do I do *specifically* so and so?**
    - No problem! Just do this.

---


## License

This project is licensed under the Apache License 2.0 and CC BY 4.0 - see the [LICENSE.md](LICENSE.md) file for details

---

## To do

you can check weekly Todo list - see the [Todo.md](Todo.md "Todo.md") file for details
