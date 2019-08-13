import { useLocalStore } from 'mobx-react-lite';

const NO_HEADER = [];

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    viewType: 'resource',
    noHeader: true,
    setSelectedMenu(data) {
      this.selectedMenu = data;
      this.noHeader = NO_HEADER.includes(menuType);
      const { menuType } = data;
    },
    get getSelectedMenu() {
      return this.selectedMenu;
    },
    changeViewType(data) {
      this.viewType = data;
    },
    get getViewType() {
      return this.viewType;
    },
    setNoHeader(data) {
      this.noHeader = data;
    },
    get getNoHeader() {
      return this.noHeader;
    },
  }));
}
