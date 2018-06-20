'use strict';

angular.module('app.diary.image', [])
    .service('DiaryImageService', function ($uibModal) {
        this.getUrl = function (imgId, w, h, keepDimensions) {
            return '/api/v1/gamediary/image/' + imgId + '/resize/' + w +
                'x' + h +
                'x' + (keepDimensions ? '1' : '0');
        };

        this.openUploadDialog = function (entry, uuid, tmp) {
            return $uibModal.open({
                templateUrl: 'diary/image/upload_image.html',
                size: 'sm',
                resolve: {
                    entry: _.constant(entry),
                    uuid: _.constant(uuid),
                    tmp: _.constant(tmp)
                },
                controller: ModalController
            }).result;
        };

        function ModalController($scope, $uibModalInstance, entry, uuid, tmp) {
            var state = {
                upload: true,
                uploading: false,
                success: false,
                error: false
            };

            $scope.onUpload = function (response) {
                state.upload = false;
                state.uploading = true;
            };

            $scope.onSuccess = function (response) {
                var newUuid = response.data;
                if (tmp) {
                    if (_.isUndefined(entry.imageIds)) {
                        entry.imageIds = [];
                    }
                    entry.imageIds.push(newUuid);
                    if (uuid) {
                        var i = entry.imageIds.indexOf(uuid);
                        if (i !== -1) {
                            entry.imageIds.splice(i, 1);
                        }
                    }
                }
                state.success = true;
                state.uploading = false;
                $uibModalInstance.close();
            };

            $scope.onError = function (response) {
                state.error = true;
                state.uploading = false;
            };

            $scope.state = state;

            var formdata = {gameDiaryEntryId: entry.id};

            if (uuid) {
                formdata.replace = uuid;
            }

            $scope.acceptTypes = 'image/jpeg, image/pjpeg, image/png';
            $scope.formdata = formdata;
            $scope.replace = uuid;
            $scope.url = tmp
                ? '/api/v1/gamediary/image/uploadtmp'
                : '/api/v1/gamediary/image/uploadFor' + (entry.isHarvest() ? 'Harvest' : 'Observation');

            $scope.close = function () {
                $uibModalInstance.close();
            };
        }
    });
