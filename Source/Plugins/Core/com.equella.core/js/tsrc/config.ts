// MUST end in /
//export const INST_URL = 'http://localhost:8080/reports/';

interface Config {
  baseUrl: string;
}

export const Config: Config = {
  baseUrl:
    typeof document == "undefined"
      ? ""
      : document.getElementsByTagName("base")[0].href!
};
